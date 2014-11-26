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

class SensorAndSoundThread extends Thread implements SensorEventListener {
	private static final boolean D = false;
	protected static boolean ORIENTATION_HORIZONTAL;
	private boolean mListening;
	private boolean mSoundPlaying;
	private Looper mLooper;
	private final SensorManager mSensorManager;
	private final Sensor mAccelerometer;
	private ArrayList<Vector3d> mRecords;
	private SoundPool mSoundPool;
	private final int mWandSoundID;
	private int mWandStreamID;
	private final EnumMap<Shape, Integer> mShapeSoundIDs;
	private final EnumMap<Buff, Integer> mBuffSoundIDs;
	private final int mNoManaSoundID;
	
	public SensorAndSoundThread(Context context, SensorManager sm, Sensor s) {
		setName("Sensor and Sound thread");
		mSensorManager = sm;
		mAccelerometer = s;
		mSoundPlaying = true;
		mListening = false;
		// Initialize sound
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mWandSoundID = mSoundPool.load(context, R.raw.magic, 1);
		mWandStreamID = -1;
		
		mShapeSoundIDs = new EnumMap<Shape, Integer>(Shape.class);
		mShapeSoundIDs.put(Shape.TRIANGLE,
				mSoundPool.load(context, R.raw.triangle_sound, 1));
		mShapeSoundIDs.put(Shape.CIRCLE,
				mSoundPool.load(context, R.raw.circle_sound, 1));
		mShapeSoundIDs.put(Shape.SHIELD,
				mSoundPool.load(context, R.raw.shield_sound, 1));
		mShapeSoundIDs.put(Shape.Z,
				mSoundPool.load(context, R.raw.z_sound, 1));
		mShapeSoundIDs.put(Shape.V,
				mSoundPool.load(context, R.raw.v_sound, 1));
		mShapeSoundIDs.put(Shape.PI,
				mSoundPool.load(context, R.raw.pi_sound, 1));
		mShapeSoundIDs.put(Shape.CLOCK,
				mSoundPool.load(context, R.raw.clock_sound, 1));
		
		mBuffSoundIDs = new EnumMap<Buff, Integer>(Buff.class);
		mBuffSoundIDs.put(Buff.HOLY_SHIELD, 
				mSoundPool.load(context, R.raw.buff_off_shield_sound, 1));
		
		mNoManaSoundID = mSoundPool.load(context, R.raw.more_mana, 1);
	}

	public void playShapeSound(Shape shape) {
		if (D) Log.e("Wizard Fight", "[shape] sound playing?: " + mSoundPlaying);
		
		Integer soundID = mShapeSoundIDs.get(shape);
		if( mSoundPlaying && soundID != null ) {
		    mSoundPool.play(soundID, 1, 1, 0, 0, 1);
		}
	}

	public void playBuffSound(Buff buff) {
		Integer soundID = mBuffSoundIDs.get(buff);
		if( mSoundPlaying && soundID != null ) {
			mSoundPool.play(soundID, 1, 1, 0, 0, 1);
		}
	}
	
	public void playNoManaSound() {
		if( mSoundPlaying ) {
			mSoundPool.play(mNoManaSoundID, 1, 1, 0, 0, 1);
		}
	}
	
	public void run() {
//        Log.e("accThread",Thread.currentThread().getName());
		Looper.prepare();
		Handler handler = new Handler();
		mLooper = Looper.myLooper();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_GAME, handler);
		Looper.loop();
	}

	public void startGettingData() {
		if (D) Log.e("Wizard Fight", "start getting data called");
		mRecords = new ArrayList<Vector3d>();
		mListening = true;
		if(!mSoundPlaying) return;
		if (mWandStreamID == -1) {
			mWandStreamID = mSoundPool.play(mWandSoundID, 0.25f, 0.25f, 0, -1, 1);
			if (D) Log.e("Wizard Fight", "wand stream id: " + mWandStreamID);
		} else {
			mSoundPool.resume(mWandStreamID);
		}
	}

	public void stopGettingData() {
		mListening = false;
		mSoundPool.pause(mWandStreamID);
	}

	public ArrayList<Vector3d> stopAndGetResult() {
		mListening = false;
		mSoundPool.pause(mWandStreamID);
		return resize(mRecords,50);
	}

	public void stopLoop() {
		mSensorManager.unregisterListener(this);
		if (mLooper != null)
			mLooper.quit();
		if (mSoundPool != null) {
			mSoundPool.release();
			mSoundPool = null;
			if (D) Log.e("Wizard Fight", "sound pool stop and release");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
//        Log.e("accThread",Thread.currentThread().getName());
		if (!mListening)
			return;
		if (mRecords.size() > 1000) return;
		double x, y, z;
		if(ORIENTATION_HORIZONTAL) {
			x = event.values[1];
			y = -event.values[0];
			z = event.values[2];
		} else {
			x = event.values[0];
			y = event.values[1];
			z = event.values[2];
		}
		double len = Math.sqrt(x * x + y * y + z * z);
		Vector3d rec = new Vector3d(x , y, z);
		mRecords.add(rec);
		if (D) Log.e("Wizard Fight", "size: " + mRecords.size());
		float amplitude = (float) len / 10 + 0.1f;
		if (amplitude > 1.0f)
			amplitude = 1.0f;
		mSoundPool.setVolume(mWandStreamID, amplitude, amplitude);
	}
    private static ArrayList<Vector3d> resize(ArrayList<Vector3d> a,int size)
    {
        if(a.size()<size)return a;
        ArrayList<Vector3d> s=new ArrayList<Vector3d>();
        double step=a.size()/(double)(size);
        for (int i = 0; i < size; i++) {
            s.add(getArrayResizeItem(a, step * i));
        }
        return s;
    }
    private static Vector3d getArrayResizeItem(ArrayList<Vector3d> a, double i){
        if(((i==((int)i))))
            return a.get((int)i);
        if(i+1>=a.size())
            return a.get(a.size()-1);
        double fPart = i % 1;
        double x= a.get((int)i).x+( a.get((int)i+1).x- a.get((int)i).x)*fPart;
        double y= a.get((int)i).y+( a.get((int)i+1).y- a.get((int)i).y)*fPart;
        double z= a.get((int)i).z+( a.get((int)i+1).z- a.get((int)i).z)*fPart;
        return new Vector3d(x,y,z);
    }
}
