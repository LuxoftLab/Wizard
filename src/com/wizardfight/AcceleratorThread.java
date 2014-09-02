package com.wizardfight;

import java.util.ArrayList;
import java.util.EnumMap;

import com.wizardfight.components.Vector3d;

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

class ToManyRecordsException extends Exception {
	private static final long serialVersionUID = 1L;
	public String toString() {
		return "To many accelerator records! ";
	}
}

public class AcceleratorThread extends Thread implements SensorEventListener {
	private static final boolean D = false;
	private boolean listening;
	private boolean soundPlaying;
	private Looper mLooper;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ArrayList<Vector3d> records;
	private SoundPool soundPool;
	private int wandSoundID;
	private int wandStreamID;
	private EnumMap<Shape, Integer> shapeSoundIDs;
	private EnumMap<Buff, Integer> buffSoundIDs;

	public AcceleratorThread(Context context, SensorManager sm, Sensor s) {
		setName("Accelerator thread");
		mSensorManager = sm;
		mAccelerometer = s;
		soundPlaying = true;
		listening = false;
		// Initialize sound
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

	public void playShapeSound(Shape shape) {
		if (D) Log.e("Wizard Fight", "[shape] sound playing?: " + soundPlaying);
		
		Integer soundID = shapeSoundIDs.get(shape);
		if( soundPlaying && soundID != null ) {
			int res = soundPool.play(soundID.intValue(), 1, 1, 0, 0, 1);
			if (D) Log.e("Wizard Fight", "play result: "  + res);
		}
	}

	public void playBuffSound(Buff buff) {
		Integer soundID = buffSoundIDs.get(buff);
		if( soundPlaying && soundID != null ) {
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
		if (D) Log.e("Wizard Fight", "start getting data called");
		records = new ArrayList<Vector3d>();
		listening = true;
		if (D) Log.e("Wizard Fight", "[data] sound playing?: " + soundPlaying);
		if(!soundPlaying) return;
		if (wandStreamID == -1) {
			wandStreamID = soundPool.play(wandSoundID, 0.25f, 0.25f, 0, -1, 1);
			if (D) Log.e("Wizard Fight", "wand stream id: " + wandStreamID);
		} else {
			soundPool.resume(wandStreamID);
		}
	}

	public void stopGettingData() {
		listening = false;
		soundPool.pause(wandStreamID);
	}

	public ArrayList<Vector3d> stopAndGetResult() {
		listening = false;
		soundPool.pause(wandStreamID);
		return records;
	}

	public void setSoundPlaying(boolean isPlaying) {
		soundPlaying = isPlaying;
	}
	
	public void stopLoop() {
		mSensorManager.unregisterListener(this);
		if (mLooper != null)
			mLooper.quit();
		if (soundPool != null) {
			soundPool.release();
			soundPool = null;
			if (D) Log.e("Wizard Fight", "sound pool stop and release");
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
		if (records.size() > 1000) return;
		double x = event.values[0];
		double y = event.values[1];
		double z = event.values[2];
		double len = Math.sqrt(x * x + y * y + z * z);
		Vector3d rec = new Vector3d(x , y, z);
		records.add(rec);
		if(records.size() > 1000) {
			try {
				throw new ToManyRecordsException();
			} catch (ToManyRecordsException e) {
				if (D) Log.e("Wizard Fight", e.toString());
			}
		}
		if (D) Log.e("Wizard Fight", "size: " + records.size());
		float amplitude = (float) len / 10 + 0.1f;
		if (amplitude > 1.0f)
			amplitude = 1.0f;
		soundPool.setVolume(wandStreamID, amplitude, amplitude);
	}
}
