package com.wizardfight;

import com.wizardfight.cast.AcceleratorThread;
import com.wizardfight.remote.WifiService;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/*
 * Main menu. Checks Bluetooth availability and 
 * checks default screen orientation (for sensor data purposes)
 */
public class MainMenu extends Activity {
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	// Intent request codes
	enum BtRequest {
		BT_CREATE_GAME, BT_JOIN_GAME
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
		
		// send context to WifiService to read player name
		WifiService.setContext(getBaseContext());

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if (mBluetoothAdapter == null) {
			// If the adapter is null, then Bluetooth is not supported
			Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_LONG)
					.show();
			findViewById(R.id.buttonCreateGame).setVisibility(View.GONE);
			findViewById(R.id.buttonJoinGame).setVisibility(View.GONE);
			findViewById(R.id.buttonDesktopConnection).setVisibility(View.GONE);
			
		} else {
			// if no player name - set as BT name
			String bluetoothName = mBluetoothAdapter.getName();
			if(bluetoothName != null) {
				SharedPreferences appPrefs = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());
				String pName = appPrefs.getString("player_name", "");
				if (pName.equals("")) {
					SharedPreferences.Editor editor = appPrefs.edit();
					editor.putString("player_name", bluetoothName);
					editor.commit();
				}
			}
		}

		//init sound
		new Thread(){
			@Override
			public void run() {
				Sound.init(getApplicationContext());
			}
		}.start();

		
		// volume buttons control multimedia volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// get default device orientation
		int screenOrientation = getDeviceDefaultOrientation();
		AcceleratorThread.ORIENTATION_HORIZONTAL =
				(screenOrientation == Configuration.ORIENTATION_LANDSCAPE);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	public void onDestroy() {
		Sound.release();
		super.onDestroy();
	}

	public void goToCreateGame(View view) {
		if (!mBluetoothAdapter.isEnabled()) {
			requestBluetooth(BtRequest.BT_CREATE_GAME);
		} else {
			BluetoothService btService = BluetoothService.getInstance();
			btService.init();
			btService.setAsServer();
			startActivity(new Intent(this, BtFightActivity.class));
		}
	}

	public void goToJoinGame(View view) {
		if (!mBluetoothAdapter.isEnabled()) {
			requestBluetooth(BtRequest.BT_JOIN_GAME);
		} else {
			startActivity(new Intent(this, DeviceListActivity.class));
		}
	}

	public void goToTestMode(View view) {
		startActivity(new Intent(this, TestFightActivity.class));
	}

	public void goToHelp(View view) {
		startActivity(new Intent(this, Tutorial.class));
	}

	public void goToSpellbook(View view) {
		startActivity(new Intent(this, Spellbook.class));
	}

	public void goToDesktopConnection(View view) {
		startActivity(new Intent(this, DesktopConnection.class));
	}

	public void goToSettings(View view) {
		startActivity(new Intent(this, WPreferences.class));
	}

	public void exit(View view) {
		BluetoothService.getInstance().release();
		finish();
	}

	void requestBluetooth(BtRequest r) {
		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, r.ordinal());
	}

	private final int getDeviceDefaultOrientation() {
		WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));

		Configuration config = getResources().getConfiguration();

		int rotation = windowManager.getDefaultDisplay().getRotation();

		if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
				|| ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
			return Configuration.ORIENTATION_LANDSCAPE;
		} else {
			return Configuration.ORIENTATION_PORTRAIT;
		}
	}

	@Override
	public void onBackPressed() {
		exit(null);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// When the request to enable Bluetooth returns
		if (resultCode != Activity.RESULT_OK) {
			// User did not enable Bluetooth or an error occured
			Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		BtRequest request = BtRequest.values()[requestCode];
		switch (request) {
		case BT_CREATE_GAME:
			BluetoothService btService = BluetoothService.getInstance();
			btService.init();
			btService.setAsServer();
			startActivity(new Intent(this, BtFightActivity.class));
			break;
		case BT_JOIN_GAME:
			startActivity(new Intent(this, DeviceListActivity.class));
			break;
		}
	}
}
