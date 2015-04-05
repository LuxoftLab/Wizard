package com.wizardfight.recognition.hmm;

import java.io.*;
import java.util.*;

import android.content.res.Resources;
import android.util.Log;

import com.wizardfight.R;
import com.wizardfight.Shape;
import com.wizardfight.components.Vector3d;

/*
 * class that runs recognition
 */
public class Recognizer {
	public final static boolean D = false;
    private static KMeansQuantizer quantizer;
    private static HMM hmm;
    private static final int TRAINED_NUM_CLUSTERS = 20;

    public static void init(Resources res) {
		quantizer = new KMeansQuantizer(TRAINED_NUM_CLUSTERS);
		// Load serialized quantizer
		try {
			ObjectInputStream is = new ObjectInputStream(
					res.openRawResource(R.raw.hmm_quantizer));
			quantizer = (KMeansQuantizer) is.readObject();
			is.close();
		} catch (Exception ex) {
			Log.e("Wizard Fight", "ERROR: Failed to load quantizer! " + ex);
			throw new RuntimeException();
		}

		hmm = new HMM();
		// Load serialized HMM
		try {
			ObjectInputStream is = new ObjectInputStream(
					res.openRawResource(R.raw.hmm_model));
			hmm = (HMM) is.readObject();
			is.close();
		} catch (Exception ex) {
			Log.e("Wizard Fight", "ERROR: Failed to load hmm! " + ex);
			throw new RuntimeException();
		}

	}

    public static Shape recognize(ArrayList<Vector3d> records) {
    	long startStamp = System.currentTimeMillis();
        quantizer.refresh();

        int[] timeSeries = new int[ records.size() ];
        double[] rec = new double[3];
        
        for (int j = 0; j < timeSeries.length ; j++) {
        	rec[0] = records.get(j).x;
        	rec[1] = records.get(j).y;
        	rec[2] = records.get(j).z;
            timeSeries[j] = quantizer.quantize(rec);
        }
        
        hmm.predict(timeSeries);

        if (D) Log.e("Wizard Fight Time", "Time: " + (System.currentTimeMillis()-startStamp) + " ms");
        return getShape(hmm.getPredictedClassLabel());
    }

    private static Shape getShape(int val) {
        Shape s;
        switch (val) {//todo
            case 1:
                s = Shape.CIRCLE;
                break;
            case 2:
                s = Shape.CLOCK;
                break;
            case 3:
                s = Shape.PI;
                break;
            case 4:
                s = Shape.SHIELD;
                break;
            case 5:
                s = Shape.TRIANGLE;
                break;
            case 6:
                s = Shape.V;
                break;
            case 7:
                s = Shape.Z;
                break;
            default:
                s = Shape.FAIL;
        }
        return s;
    }
}
