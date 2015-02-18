package com.wizardfight;

import java.util.ArrayList;

import com.wizardfight.accrecognizer.AccRecognition;
import com.wizardfight.components.Vector3d;
import com.wizardfight.recognition.Recognizer;

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
	protected static final boolean D = false;
	protected static String TAG = "Wizard Fight";
	protected final Boolean sync=true;
	// Accelerator Thread link
	protected SensorAndSoundThread mSensorAndSoundThread = null; 
	// Last touch action code
	protected int mLastTouchAction; 

	protected boolean mIsInCast = false; 
	protected boolean mIsCastAbilityBlocked = false; 
	// The Handler that gets information back from the BluetoothChatService
	protected Handler mHandler;
	protected Handler mTimer=new Handler();

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
	private final Runnable mainResume = new Runnable() {
		public void run() {
			startNewSensorAndSound();
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		mLastTouchAction = MotionEvent.ACTION_UP;
		mTimer.postDelayed(mainResume,100);
		if (D)
			Log.e(TAG, "accelerator ran");
	}
	protected  void startNewSensorAndSound(){
			mSensorAndSoundThread = new SensorAndSoundThread(this, ((SensorManager) getSystemService(Context.SENSOR_SERVICE)));
			mSensorAndSoundThread.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (D)
            Log.e(TAG, "- ON PAUSE -");
        stopSensorAndSound();
	}

	protected abstract Handler getHandler();

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
			Log.e(TAG, "START GETTING DATA");
			if(mSensorAndSoundThread!=null) {
				mSensorAndSoundThread.startGettingData();
				mIsInCast = true;
			}
		} else {
			Log.e(TAG, "END GETTING DATA");
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
