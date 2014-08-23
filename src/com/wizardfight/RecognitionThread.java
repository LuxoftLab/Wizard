package com.wizardfight;

import java.util.ArrayList;
import java.util.EnumMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import com.wizardfight.WizardFight.AppMessage;
import com.wizardfight.components.*;
import com.wizardfight.recognition.Recognizer;

import android.os.Handler;
import android.util.Log;

public class RecognitionThread extends Thread {
	private Handler mHandler;
	private ArrayList<Vector3d> records;
	private Shape shape;
    private SoundPool soundPool;
    private int soundID1;
    private int streamID;
    AcceleratorThread acceleratorThread;

	
	public RecognitionThread(Handler mainHandler, ArrayList<Vector3d> recs,AcceleratorThread acceleratorThread) {
		setName("Recognition thread");
		mHandler = mainHandler;
		records = recs;
        this.acceleratorThread=acceleratorThread;
	}
	
	public void run() {
		try {
			Log.e("Wizard fight", "Recognition thread begin");
			Shape shape = Recognizer.recognize(records);
            acceleratorThread.playShapeSound(shape);
            Log.e("Wizard Fight", streamID + " "+soundID1+"");
			FightMessage message = new FightMessage(shape);
			mHandler.obtainMessage(AppMessage.MESSAGE_FROM_SELF.ordinal(), 0, 0, message)
        		.sendToTarget();
			Log.e("Wizard fight", "Recognition thread end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
