package com.wizardfight;

import java.util.ArrayList;
import com.wizardfight.recognition.accrecognizer.AccRecognizer;
import com.wizardfight.components.Vector3d;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

/* 
 * Thread that listens to accelerometer and gathers data 
 * when its needed. Also plays music
 */
class AcceleratorThread extends Thread implements SensorEventListener {
	private static final boolean D = false;
	protected static boolean ORIENTATION_HORIZONTAL;
	private boolean mListening;
	
	private Looper mLooper;
	private final Context mContext;
	private final SensorManager mSensorManager;
	private final Handler mFightHandler;
	private Sensor mAccelerometer;
	private ArrayList<Vector3d> mRecords;

	public AcceleratorThread(Context context, SensorManager sm, Handler fightHandler) {
		setName("Sensor and Sound thread");
		mContext = context;
		mSensorManager = sm;
		mFightHandler = fightHandler;
	}

	public void run() {
		long t1 = System.currentTimeMillis();
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sound.setPlaying(true);
		mListening = false;
		
		Looper.prepare();

		Handler handler = new Handler();
		mLooper = Looper.myLooper();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_GAME, handler);

		Looper.loop();
		
	}

	public void startGettingData() {
		mRecords = new ArrayList<Vector3d>();
		mListening = true;
		if (!Sound.isPlaying())
			return;
		Sound.playWandSound();
	}

	public void stopGettingData() {
		long t1 = System.currentTimeMillis();
		mListening = false;
		Sound.stopWandSound();
	}

	public ArrayList<Vector3d> stopAndGetResult() {
		long t1 = System.currentTimeMillis();
		mListening = false;
		Sound.stopWandSound();
		return Vector3d.squeeze(mRecords, AccRecognizer.Speed.SLOW.size);
	}

	public void stopLoop() {
		long t1 = System.currentTimeMillis();
		mSensorManager.unregisterListener(this);
		if (mLooper != null)
			mLooper.quit();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!mListening)
			return;
		if (mRecords.size() > 1000)
			return;
		double x, y, z;
		if (ORIENTATION_HORIZONTAL) {
			x = event.values[1];
			y = -event.values[0];
			z = event.values[2];
		} else {
			x = event.values[0];
			y = event.values[1];
			z = event.values[2];
		}
		double len = Math.sqrt(x * x + y * y + z * z);
		Vector3d rec = new Vector3d(x, y, z);
		mRecords.add(rec);

		float amplitude = (float) len / 10 + 0.1f;
		if (amplitude > 1.0f)
			amplitude = 1.0f;
		
		Sound.setWandVolume(amplitude);
	}
}
