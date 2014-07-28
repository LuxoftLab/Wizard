package com.example.wizard1;

import java.util.ArrayList;

import com.example.wizard1.components.Vector4d;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AcceleratorThread extends Thread implements SensorEventListener {
	private boolean listening;
	private Looper mLooper;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ArrayList<Vector4d> records;
	private double gravity;
	private SoundPool soundPool;
	private int soundID1;
	private int streamID;

	public AcceleratorThread(Context context, SensorManager sm, Sensor s, double c) {
		this.gravity = c;
		setName("Accelerator thread");
		mSensorManager = sm;
		mAccelerometer = s;
		listening = false;
		// Initialize sound
		Log.e("Wizard Fight", "Sound pool is null? : " + (soundPool == null));
		if (soundPool == null) {
			soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			soundID1 = soundPool.load(context, R.raw.magic, 1);
			streamID = -1;
		}
	}

	public void run() {
		Looper.prepare();
		Handler handler = new Handler();
		mLooper = Looper.myLooper();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_GAME, handler);
		Looper.loop();
	}

	public void startGettingData() {
		records = new ArrayList<Vector4d>();
		listening = true;
		if (streamID == -1) {
			streamID = soundPool.play(soundID1, 0.25f, 0.25f, 0, -1, 1);
		} else {
			soundPool.resume(streamID);
		}
	}

	public void stopGettingData() {
		listening = false;
		soundPool.pause(streamID);
	}

	public ArrayList<Vector4d> stopAndGetResult() {
		listening = false;
		soundPool.pause(streamID);
		return records;
	}

	public double recountGravity() {
		gravity = 0.0;
		for (Vector4d v : records) {
			gravity += v.getLength();
		}
		if (records.size() != 0)
			gravity /= records.size();
		return gravity;
	}

	public void stopLoop() {
		mSensorManager.unregisterListener(this);
		if (mLooper != null)
			mLooper.quit();
		if (soundPool != null && streamID != -1) {
			soundPool.stop(streamID);
			soundPool.release();
			soundPool = null;
			Log.e("Wizard Fight", "sound pool stop and release");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!listening)
			return;

		double x = event.values[0];
		double y = event.values[1];
		double z = event.values[2];
		double len = Math.sqrt(x * x + y * y + z * z);
		Vector4d rec = new Vector4d(x - gravity * (x / len), y - gravity
				* (y / len), z - gravity * (z / len), event.timestamp);
		records.add(rec);
		float amplitude = (float)len/10 + 0.1f;
		if(amplitude > 1.0f) amplitude = 1.0f;
		soundPool.setVolume(streamID, amplitude, amplitude);
	}
}
