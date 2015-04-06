package com.wizardfight.cast;

import java.util.ArrayList;

import com.wizardfight.recognition.accrecognizer.AccRecognition;
import com.wizardfight.components.Vector3d;
import com.wizardfight.recognition.hmm.Recognizer;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

/*
 * Class listens to screen touches, activates accelerator 
 * data gathering and activates the recognition
 */
public abstract class CastActivity extends Activity {
	public static final boolean D = true;
	private static final int MIN_RECORDS_SIZE = 5;
	protected static String TAG = "Wizard Fight";
	// Accelerator Thread link
	protected AcceleratorThread mAcceleratorThread = null; 
	// Last touch action code
	protected int mLastTouchAction; 

	protected boolean mIsInCast = false; 
	protected boolean mIsCastAbilityBlocked = false; 
	// The Handler that gets information back from the BluetoothChatService
	protected Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// Init recognition resources
		Recognizer.init(getResources());
		AccRecognition.init(getResources());
		// Get sensors
		mHandler = getHandler();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "--- CAST ACTIVITY ON RESUME ---");
		mLastTouchAction = MotionEvent.ACTION_UP;
		startNewSensorAndSound();
		if (D)
			Log.e(TAG, "accelerator ran");
	}
	
	protected  void startNewSensorAndSound(){
			mAcceleratorThread = new AcceleratorThread(this, 
					((SensorManager) getSystemService(Context.SENSOR_SERVICE)), mHandler);
			mAcceleratorThread.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (D)
            Log.e(TAG, "--- CAST ACTIVITY ON PAUSE ---");
        stopSensorAndSound();
	}

	protected abstract Handler getHandler();

	protected void stopSensorAndSound() {
		if (D) Log.e("Wizard Fight", "stop sensor and sound called");
		// stop cast if its started
		if (mIsInCast) {
			mIsInCast = false;
			mIsCastAbilityBlocked = false;
		}

		if (mAcceleratorThread != null) {
			// stop cast
			mAcceleratorThread.stopGettingData();
			// unregister accelerator listener and end stop event loop
			if (D)
				Log.e(TAG, "accelerator thread try to stop loop");
			mAcceleratorThread.stopLoop();
			mAcceleratorThread = null;
		}
	}

	protected void buttonClick() {
		if (mIsCastAbilityBlocked)
			return;

		if (!mIsInCast) {
			if (D) Log.e(TAG, "START GETTING DATA");
			if(mAcceleratorThread!=null) {
				mAcceleratorThread.startGettingData();
				mIsInCast = true;
			}
		} else {
			if (D)Log.e(TAG, "END GETTING DATA");
			mIsCastAbilityBlocked = true;

			ArrayList<Vector3d> records = mAcceleratorThread
					.stopAndGetResult();
			mIsInCast = false;

			if (records.size() >= MIN_RECORDS_SIZE) {
				new RecognitionThread(mHandler, records).start();
			} else {
				// if shord record - don`t recognize & unblock
				mIsCastAbilityBlocked = false;
			}
		}
	}
}
