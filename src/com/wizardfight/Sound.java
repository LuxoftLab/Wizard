package com.wizardfight;

import java.util.EnumMap;

import com.wizardfight.fight.Buff;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class Sound {
	private static boolean mSoundPlaying;
	private static SoundPool mSoundPool;
	private static int mWandSoundID;
	private static int mWandStreamID;
	private static EnumMap<Shape, Integer> mShapeSoundIDs;
	private static EnumMap<Buff, Integer> mBuffSoundIDs;
	private static int mNoManaSoundID;

	static {
		mBuffSoundIDs = new EnumMap<Buff, Integer>(Buff.class);
		mShapeSoundIDs = new EnumMap<Shape, Integer>(Shape.class);
	}
	
	public static void init(Context context) {
//		long t1 = System.currentTimeMillis();
		// Initialize sound
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		mWandSoundID = mSoundPool.load(context, R.raw.magic, 1);
		mWandStreamID = -1;
		
		mShapeSoundIDs.put(Shape.TRIANGLE,
				mSoundPool.load(context, R.raw.triangle_sound, 1));
		mShapeSoundIDs.put(Shape.CIRCLE,
				mSoundPool.load(context, R.raw.circle_sound, 1));
		mShapeSoundIDs.put(Shape.SHIELD,
				mSoundPool.load(context, R.raw.shield_sound, 1));
		mShapeSoundIDs.put(Shape.Z, mSoundPool.load(context, R.raw.z_sound, 1));
		mShapeSoundIDs.put(Shape.V, mSoundPool.load(context, R.raw.v_sound, 1));
		mShapeSoundIDs.put(Shape.PI,
				mSoundPool.load(context, R.raw.pi_sound, 1));
		mShapeSoundIDs.put(Shape.CLOCK,
				mSoundPool.load(context, R.raw.clock_sound, 1));
		mShapeSoundIDs.put(Shape.FAIL, 
				mSoundPool.load(context, R.raw.fail_sound, 1));

		
		mBuffSoundIDs.put(Buff.HOLY_SHIELD,
				mSoundPool.load(context, R.raw.buff_off_shield_sound, 1));

		mNoManaSoundID = mSoundPool.load(context, R.raw.more_mana, 1);
//		Log.e("Wizard Fight", "sound load time: " + (System.currentTimeMillis() - t1));
	}
	
	public static void playShapeSound(Shape shape) {
		Integer soundID = mShapeSoundIDs.get(shape);
		if (mSoundPlaying && soundID != null) {
			mSoundPool.play(soundID, 1, 1, 0, 0, 1);
		}
	}

	public static void playBuffSound(Buff buff) {
		Integer soundID = mBuffSoundIDs.get(buff);
		if (mSoundPlaying && soundID != null) {
			mSoundPool.play(soundID, 1, 1, 0, 0, 1);
		}
	}

	public static void playNoManaSound() {
		if (mSoundPlaying) {
			mSoundPool.play(mNoManaSoundID, 1, 1, 0, 0, 1);
		}
	}
	
	public static void playWandSound() {
		if (mWandStreamID == -1) {
			mWandStreamID = mSoundPool.play(mWandSoundID, 0.25f, 0.25f, 0, -1,
					1);
		} else {
			mSoundPool.resume(mWandStreamID);
		}
	}
	
	public static void setWandVolume(float amplitude) {
		mSoundPool.setVolume(mWandStreamID, amplitude, amplitude);
	}
	
	public static void stopWandSound() {
		mSoundPool.pause(mWandStreamID);
	}

	public static void release() {
		if (mSoundPool != null) {
			mSoundPool.release();
			mSoundPool = null;
		}
	}
	
	public static boolean isPlaying() { return mSoundPlaying; }
	public static void setPlaying(boolean isPlaying) { mSoundPlaying = isPlaying; }
}
