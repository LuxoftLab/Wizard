package com.example.wizard1;

import java.util.ArrayList;
import java.util.EnumMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import com.example.wizard1.WizardFight.AppMessage;
import com.example.wizard1.components.*;

import android.os.Handler;
import android.util.Log;

public class RecognitionThread extends Thread {
	private Handler mHandler;
	private ArrayList<Vector4d> records;
	private Shape shape;
    private SoundPool soundPool;
    private int soundID1;
    private int streamID;
    AcceleratorThread acceleratorThread;

	
	public RecognitionThread(Handler mainHandler, ArrayList<Vector4d> recs,AcceleratorThread acceleratorThread) {
		setName("Recognition thread");
		mHandler = mainHandler;
		records = recs;
        this.acceleratorThread=acceleratorThread;
	}
	
	public void run() {
		try {
			Log.e("Wizard fight", "Recognition thread begin");
			//ArrayList<Vector2d> projection = PathCalculator.calculateTrajectory(records);
			Shape shape = Recognition.recognize(records);
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
