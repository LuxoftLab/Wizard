package com.wizardfight;

import java.util.ArrayList;
import com.wizardfight.FightActivity.AppMessage;
import com.wizardfight.components.*;
import com.wizardfight.accrecognizer.AccRecognition;
import com.wizardfight.recognition.Recognizer;
import android.os.Handler;
import android.util.Log;

/*
 * Thread that runs recognition and sends result via handler
 */
class RecognitionThread extends Thread {
	private final static boolean D = false;
	private final Handler mHandler;
	private final ArrayList<Vector3d> mRecords;

	
	public RecognitionThread(Handler mainHandler, ArrayList<Vector3d> recs) {
		setName("Recognition thread");
		mHandler = mainHandler;
		mRecords = recs;
	}
	
	public void run() {
		try {
			if (D) Log.e("Wizard fight", "Recognition thread begin");
			String shapeStr = AccRecognition.recognize(mRecords).toUpperCase();
			Shape accShape, shape;
			boolean goodDensity = AccRecognition.isGoodDensity();
			try {
				accShape = Shape.valueOf(shapeStr.toUpperCase());
			} catch(Exception e) {
				accShape = Shape.FAIL;
			}
			
			if(goodDensity) {
				shape = accShape;
			} else {
				Shape hmmShape = Recognizer.recognize(mRecords);
				shape = (accShape == hmmShape)? accShape : Shape.FAIL; 
			}
			
			FightMessage message = new FightMessage(shape);
			mHandler.obtainMessage(AppMessage.MESSAGE_FROM_SELF.ordinal(), 0, 0, message)
        		.sendToTarget();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
