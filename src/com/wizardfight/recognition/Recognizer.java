package com.wizardfight.recognition;

import java.io.*;
import java.util.*;

import android.content.res.Resources;
import android.util.Log;

import com.wizardfight.R;
import com.wizardfight.Shape;
import com.wizardfight.components.Vector3d;

public class Recognizer {
    private static KMeansQuantizer quantizer;
    private static HMM hmm;

    public static void init(Resources res, String recoFileName) {
		// The input to the HMM must be a quantized discrete value
		// We therefore use a KMeansQuantizer to covert the N-dimensional
		// continuous data into 1-dimensional discrete data
		int quantizerFileId = R.raw.hmm_quantizer;
		int modelFileId =  R.raw.hmm_model;
		
		// choose recognition base 
		if(recoFileName.equals("23.08.14")) {
			Log.e("Wizard Fight", "recognition 23.08.14");
			quantizerFileId = R.raw.hmm_quantizer;
			modelFileId = R.raw.hmm_model;
		} else if (recoFileName.equals("07.09.14")){
			Log.e("Wizard Fight", "recognition 07.09.14");
			quantizerFileId = R.raw.hmm_quantizer_0709;
			modelFileId = R.raw.hmm_model_0709;
		} else {
			Log.e("Wizard Fight", "recognition 23.08.14");
		}

    	final int NUM_SYMBOLS = 20; // 10 - default
		quantizer = new KMeansQuantizer(NUM_SYMBOLS);

		// Load quantizer from serialized file
		try {
			ObjectInputStream is = new ObjectInputStream(
					res.openRawResource(quantizerFileId));
			quantizer = (KMeansQuantizer) is.readObject();
			is.close();
		} catch (Exception ex) {
			System.err.println("ERROR: Failed to load quantizer! " + ex);
		}

		// Create a new HMM instance
		hmm = new HMM();
		// Load the HMM model from a file
		try {
			ObjectInputStream is = new ObjectInputStream(
					res.openRawResource(modelFileId));
			hmm = (HMM) is.readObject();
			is.close();
		} catch (Exception ex) {
			System.err.println("ERROR: Failed to load hmm! " + ex);
		}

	}

    public static Shape recognize(ArrayList<Vector3d> records) {
    	long startStamp = System.currentTimeMillis();
        quantizer.refresh();
        // Load recognition data
        TimeSeriesClassificationData testData = new TimeSeriesClassificationData();
        testData.loadDatasetFromRecords(records);

        TimeSeriesClassificationData quantizedTestData = new TimeSeriesClassificationData(1);
        int classLabel = testData.getData().getClassLabel();
        MatrixDouble quantizedSample = new MatrixDouble();

        for (int j = 0; j < testData.getData().getLength(); j++) {
            quantizer.quantize(testData.getData().getData().getRowVector(j));
            quantizedSample.push_back(quantizer.getFeatureVector());
        }

        if (!quantizedTestData.addSample(classLabel, quantizedSample)) {
            System.out.println("ERROR: Failed to quantize training data!");
            return Shape.FAIL;
        }

        hmm.predict(quantizedTestData.getData().getData());

        Log.e("Wizard Fight", "Time: " + (System.currentTimeMillis()-startStamp) + " ms");
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
