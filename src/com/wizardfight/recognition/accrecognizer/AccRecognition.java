package com.wizardfight.recognition.accrecognizer;

import java.io.ObjectInputStream;
import java.util.ArrayList;

import android.content.res.Resources;
import android.util.Log;

import com.wizardfight.components.Vector3d;
import com.wizardfight.R;

public class AccRecognition {
	public static boolean D = true;
	private static AccRecognizer recognizer;
	public static void init(Resources res) {
		recognizer = new AccRecognizer();
		try {
			ObjectInputStream is = new ObjectInputStream(
					res.openRawResource(R.raw.acc_model));
			recognizer = (AccRecognizer) is.readObject();
			is.close();
			if (D) Log.e("AccRecognition", "recognizer is loaded");
		} catch (Exception ex) {
			if (D) Log.e("AccRecognition", "ERROR: Failed to load acc recognizer ! " + ex);
			throw new RuntimeException();
		}
	}
	public static String recognize(ArrayList<Vector3d> records) {
		return recognizer.recognize(records);
	}
	
	public static double getDensity() {
		return recognizer.getDensity();
	}
	public static double getAccuracy() {
		return recognizer.getAccuracy();
	}
	public static boolean isGoodDensity() {
		return recognizer.isGoodDensity();
	}
}
