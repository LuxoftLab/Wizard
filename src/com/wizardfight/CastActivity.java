package com.wizardfight;

import java.util.ArrayList;

import com.wizardfight.components.Vector3d;
import com.wizardfight.recognition.Recognizer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;

public abstract class CastActivity extends Activity {
	protected static final boolean D = true;
	protected static String TAG = "Wizard Fight";

	// Objects referred to accelerometer
	protected SensorManager mSensorManager = null; // !!!!
	protected Sensor mAccelerometer = null; // !!!!
	// Accelerator Thread link
	protected SensorAndSoundThread mSensorAndSoundThread = null; // !!!!
	// Last touch action code
	protected int mLastTouchAction; // !!!!

	protected boolean mIsInCast = false; // !!!!
	protected boolean mIsCastAbilityBlocked = false; // !!!!
	// The Handler that gets information back from the BluetoothChatService
	protected Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// Init recognition resources
		SharedPreferences appPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String rType = appPrefs.getString("recognition_type", "");
		Log.e("Wizard Fight", rType);
		Recognizer.init(getResources(), rType);

		// Get sensors
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		initHandler();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		mLastTouchAction = MotionEvent.ACTION_UP;
		mSensorAndSoundThread = new SensorAndSoundThread(this, mSensorManager,
				mAccelerometer);
		mSensorAndSoundThread.start();
		if (D)
			Log.e(TAG, "accelerator ran");
	}

	@Override
	public void onPause() {
		super.onPause();
		if (D)
            Log.e(TAG, "- ON PAUSE -");
        stopSensorAndSound();
	}

	protected void initHandler() {}

	protected void stopSensorAndSound() {
		Log.e("Wizard Fight", "stop sensor and sound called");
		// stop cast if its started
		if (mIsInCast) {
			mIsInCast = false;
			mIsCastAbilityBlocked = false;
		}

		if (mSensorAndSoundThread != null) {
			// stop cast
			mSensorAndSoundThread.stopGettingData();
			// unregister accelerator listener and end stop event loop
			if (D)
				Log.e(TAG, "accelerator thread try to stop loop");
			mSensorAndSoundThread.stopLoop();
			mSensorAndSoundThread = null;
		}
	}

	protected void buttonClick() {
		if (mIsCastAbilityBlocked)
			return;

		if (!mIsInCast) {
			mSensorAndSoundThread.startGettingData();
			mIsInCast = true;

		} else {
			mIsCastAbilityBlocked = true;

			ArrayList<Vector3d> records = mSensorAndSoundThread
					.stopAndGetResult();
			mIsInCast = false;

			if (records.size() > 10) {
				new RecognitionThread(mHandler, records).start();
			} else {
				// if shord record - don`t recognize & unblock
				mIsCastAbilityBlocked = false;
			}
		}
	}
}
