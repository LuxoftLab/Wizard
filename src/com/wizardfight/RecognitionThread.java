package com.wizardfight;

import java.util.ArrayList;
import com.wizardfight.FightActivity.AppMessage;
import com.wizardfight.components.*;
import com.wizardfight.recognition.Recognizer;

import android.os.Handler;
import android.util.Log;

class RecognitionThread extends Thread {
	private final Handler mHandler;
	private final ArrayList<Vector3d> mRecords;

	
	public RecognitionThread(Handler mainHandler, ArrayList<Vector3d> recs) {
		setName("Recognition thread");
		mHandler = mainHandler;
		mRecords = recs;
	}
	
	public void run() {
		try {
			Log.e("Wizard fight", "Recognition thread begin");
			Shape shape = Recognizer.recognize(mRecords);
			FightMessage message = new FightMessage(shape);
			mHandler.obtainMessage(AppMessage.MESSAGE_FROM_SELF.ordinal(), 0, 0, message)
        		.sendToTarget();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
