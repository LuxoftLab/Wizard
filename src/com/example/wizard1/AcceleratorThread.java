package com.example.wizard1;

import java.util.ArrayList;
import java.util.EnumMap;

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
import android.util.Log;

public class AcceleratorThread extends Thread implements SensorEventListener {
	private boolean listening;
	private Looper mLooper;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ArrayList<Vector4d> records;
	private double gravity;
	private SoundPool soundPool;
	private int wandSoundID;
	private int wandStreamID;
	private EnumMap<Shape, Integer> shapeSoundIDs;
	private EnumMap<Buff, Integer> buffSoundIDs;
	//soundPool.play(soundID, 1, 1, 0, 0, 1);

	public AcceleratorThread(Context context, SensorManager sm, Sensor s,
			double c) {
		this.gravity = c;
		setName("Accelerator thread");
		mSensorManager = sm;
		mAccelerometer = s;
		listening = false;
		// Initialize sound
		Log.e("Wizard Fight", "Sound pool is null? : " + (soundPool == null));
		if (soundPool == null) {
			soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
			wandSoundID = soundPool.load(context, R.raw.magic, 1);
			wandStreamID = -1;
			
			shapeSoundIDs = new EnumMap<Shape, Integer>(Shape.class);
			shapeSoundIDs.put(Shape.TRIANGLE,
					soundPool.load(context, R.raw.triangle_sound, 1));
			shapeSoundIDs.put(Shape.CIRCLE,
					soundPool.load(context, R.raw.circle_sound, 1));
			shapeSoundIDs.put(Shape.SHIELD,
					soundPool.load(context, R.raw.shield_sound, 1));
			shapeSoundIDs.put(Shape.Z,
					soundPool.load(context, R.raw.z_sound, 1));
			shapeSoundIDs.put(Shape.V,
					soundPool.load(context, R.raw.v_sound, 1));
			shapeSoundIDs.put(Shape.PI,
					soundPool.load(context, R.raw.pi_sound, 1));
			shapeSoundIDs.put(Shape.CLOCK,
					soundPool.load(context, R.raw.clock_sound, 1));
			
			buffSoundIDs = new EnumMap<Buff, Integer>(Buff.class);
			buffSoundIDs.put(Buff.HOLY_SHIELD, 
					soundPool.load(context, R.raw.buff_off_shield_sound, 1));
		}
	}

	public void playShapeSound(Shape shape) {
		Integer soundID = shapeSoundIDs.get(shape);
		if( soundID != null) {
			soundPool.play(soundID.intValue(), 1, 1, 0, 0, 1);
		}
	}

	public void playBuffSound(Buff buff) {
		Integer soundID = buffSoundIDs.get(buff);
		if( soundID != null) {
			soundPool.play(soundID.intValue(), 1, 1, 0, 0, 1);
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
		if (wandStreamID == -1) {
			wandStreamID = soundPool.play(wandSoundID, 0.25f, 0.25f, 0, -1, 1);
			Log.e("Wizard Fight", wandStreamID + " " + wandSoundID + "");
		} else {
			soundPool.resume(wandStreamID);
		}
	}

	public void stopGettingData() {
		listening = false;
		soundPool.pause(wandStreamID);
	}

	public ArrayList<Vector4d> stopAndGetResult() {
		listening = false;
		soundPool.pause(wandStreamID);
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
		if (soundPool != null && wandStreamID != -1) {
			soundPool.stop(wandStreamID);
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
		float amplitude = (float) len / 10 + 0.1f;
		if (amplitude > 1.0f)
			amplitude = 1.0f;
		soundPool.setVolume(wandStreamID, amplitude, amplitude);
	}
}
