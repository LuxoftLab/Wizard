package com.wizardfight;

import java.util.ArrayList;
import com.wizardfight.WizardFight.AppMessage;
import com.wizardfight.components.*;
import com.wizardfight.recognition.Recognizer;

import android.os.Handler;
import android.util.Log;

public class RecognitionThread extends Thread {
	private Handler mHandler;
	private ArrayList<Vector3d> records;

	
	public RecognitionThread(Handler mainHandler, ArrayList<Vector3d> recs) {
		setName("Recognition thread");
		mHandler = mainHandler;
		records = recs;
	}
	
	public void run() {
		try {
			Log.e("Wizard fight", "Recognition thread begin");
			Shape shape = Recognizer.recognize(records);
			FightMessage message = new FightMessage(shape);
			mHandler.obtainMessage(AppMessage.MESSAGE_FROM_SELF.ordinal(), 0, 0, message)
        		.sendToTarget();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
