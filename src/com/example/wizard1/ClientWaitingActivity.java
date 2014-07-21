package com.example.wizard1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ClientWaitingActivity extends Activity {
	// For debugging
	private static final String TAG = "Wizard Fight";
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Messages types
	enum CWMessage {
		MESSAGE_STATE_CHANGE, MESSAGE_READ, MESSAGE_WRITE, MESSAGE_DEVICE_NAME, MESSAGE_TOAST;
	}

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Member object for bluetooth services
	private BluetoothChatService mChatService = null;
	// Shows if we can stop bluetooth services 
	private boolean mIsCanStopBtService = true;
	// GUI objects
	private TextView mDebug1;
	private TextView mDebug2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_waiting);

		mDebug1 = (TextView) findViewById(R.id.cw_debug1);
		mDebug2 = (TextView) findViewById(R.id.cw_debug2);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mChatService == null) setup();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		Log.e(TAG, "+ ON RESUME +");
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mIsCanStopBtService && mChatService != null) {
			mChatService.stop();
		}
		Log.e(TAG, "--- ON DESTROY ---");
	}

	public void setup() {
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = BluetoothChatService.getInstance();
		mChatService.init();
		mChatService.setHandler(mHandler);
	}

	private void startFight() {
		// mark that we still need bt service
		mIsCanStopBtService = false;
		startActivity(new Intent(this, WizardFight.class));
	}
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			CWMessage msgType = CWMessage.values()[msg.what];
			switch (msgType) {
			case MESSAGE_STATE_CHANGE:
				Log.e(TAG, "cw MESSAGE_STATE_CHANGE: " + msg.arg1);
				
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mDebug1.setText(R.string.title_connected_to);
					mDebug1.append(mConnectedDeviceName);
					// STARTING WIZARD FIGHT ACTIVITY HERE
					startFight();
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mDebug1.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mDebug1.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_DEVICE_NAME:
				Log.e(TAG, "cw MESSAGE_DEVICE_NAME: " + msg.arg1);
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
            	Log.e(TAG, "cw MESSAGE_TOAST: " + msg.arg1);
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
			}
		}
	};
}
