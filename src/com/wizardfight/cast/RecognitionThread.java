package com.wizardfight.cast;

import java.util.ArrayList;

import com.wizardfight.Shape;
import com.wizardfight.components.*;
import com.wizardfight.fight.FightMessage;
import com.wizardfight.fight.FightCore.HandlerMessage;
import com.wizardfight.fight.FightMessage.FightAction;
import com.wizardfight.fight.FightMessage.Target;
import com.wizardfight.recognition.accrecognizer.AccRecognition;
import com.wizardfight.recognition.hmm.Recognizer;

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
			mHandler.obtainMessage(HandlerMessage.HM_FROM_SELF.ordinal(), 0, 0, message)
        		.sendToTarget();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
