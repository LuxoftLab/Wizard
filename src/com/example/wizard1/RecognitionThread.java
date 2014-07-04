package com.example.wizard1;

import java.util.ArrayList;

import com.example.wizard1.WizardFight.AppMessage;
import com.example.wizard1.components.*;

import android.os.Handler;
import android.util.Log;

public class RecognitionThread extends Thread {
	private Handler mHandler;
	private ArrayList<Vector4d> records;
	private Shape shape;
	
	public RecognitionThread(Handler mainHandler, ArrayList<Vector4d> recs) {
		setName("Recognition thread");
		mHandler = mainHandler;
		records = recs;
	}
	
	public void run() {
		try {
			Log.e("Wizard fight", "Recognition thread begin");
			ArrayList<Vector2d> projection = PathCalculator.calculateTrajectory(records);
			Shape shape = Recognition.recognize(projection, mHandler);
			FightMessage message = new FightMessage(shape);
			mHandler.obtainMessage(AppMessage.MESSAGE_FROM_SELF.ordinal(), 0, 0, message)
        		.sendToTarget();
			Log.e("Wizard fight", "Recognition thread end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
