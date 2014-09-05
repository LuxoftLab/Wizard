package com.wizardfight;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainMenu extends Activity {
	// Debugging
	private static final String TAG = "Wizard Fight";
	private static final boolean D = true;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	private boolean mIsUserCameWithBt;
	// Intent request codes
	enum BtRequest {
		BT_CREATE_GAME, BT_JOIN_GAME;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* Full screen */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main_menu);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		mIsUserCameWithBt = mBluetoothAdapter.isEnabled();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	public void goToCreateGame(View view) {
		if(!mBluetoothAdapter.isEnabled()) {
			requestBluetooth(BtRequest.BT_CREATE_GAME);
		} else {
			BluetoothService btService = BluetoothService.getInstance();
			btService.init();
			btService.setAsServer();
			startActivity(new Intent(this, WizardFight.class));
		}
	}

	public void goToJoinGame(View view) {
		if(!mBluetoothAdapter.isEnabled()) {
			requestBluetooth(BtRequest.BT_JOIN_GAME);
		} else {
			startActivity(new Intent(this, DeviceListActivity.class));
		}
	}

	public void goToTestMode(View view) {
		BluetoothService btService = BluetoothService.getInstance();
		btService.init();
		Intent i = new Intent(this, WizardFight.class);
		i.putExtra("IS_ENEMY_BOT", true);
		startActivity(i);
	}

	public void goToHelp(View view) {
		startActivity(new Intent(this, Tutorial.class));
	}

	public void goToSpellbook(View view) {
		startActivity(new Intent(this, Spellbook.class));
	}

	public void Exit(View view) {
		BluetoothService.getInstance().release();
		// return BT state to last one in 
		if(!mIsUserCameWithBt && mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable();
		}
		finish();
	}

	public void requestBluetooth(BtRequest r) {
		Intent enableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, r.ordinal());
	}

	@Override
	public void onBackPressed() {
		Exit(null);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);
		// When the request to enable Bluetooth returns
		if (resultCode != Activity.RESULT_OK) {
			// User did not enable Bluetooth or an error occured
			if (D)
				Log.d(TAG, "BT not enabled");
			Toast.makeText(this, R.string.bt_not_enabled,
					Toast.LENGTH_SHORT).show();
			return;
		}

		BtRequest request = BtRequest.values()[requestCode];
		switch (request) {
		case BT_CREATE_GAME:
			BluetoothService btService = BluetoothService.getInstance();
			btService.init();
			btService.setAsServer();
			startActivity(new Intent(this, WizardFight.class));
			break;
		case BT_JOIN_GAME:
			startActivity(new Intent(this, DeviceListActivity.class));
			break;
		}
	}
}
