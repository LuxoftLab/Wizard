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
					res.openRawResource(R.raw.hmm_quantizer_0709));
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
					res.openRawResource(R.raw.hmm_model_0709));
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

    static Shape getShape(int val) {
        Shape s;
        switch (val) {
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

    // Converting class id into label
    static void printLabel(int val) {
        System.out.print("Figure is ");
        switch (val) {
            case 1:
                System.out.print("circle");
                break;
            case 2:
                System.out.print("clock");
                break;
            case 3:
                System.out.print("pi");
                break;
            case 4:
                System.out.print("shield");
                break;
            case 5:
                System.out.print("triangle");
                break;
            case 6:
                System.out.print("v");
                break;
            case 7:
                System.out.print("z");
                break;
        }
        System.out.println();
    }

    public static void writeSerialized() {
        Loader loader = new Loader();
        // Load quantizer from serialized file
        try {
            ObjectOutputStream os = new ObjectOutputStream(
                    new FileOutputStream(new File("HMMQuantizer.ser")));
            loader.loadQuantizerFromFile("HMMQuantizer.txt");
            os.writeObject(loader.quantizer);
            os.close();
        } catch (Exception ex) {
            System.err.println("ERROR: Failed to write quantizer! " + ex);
        }

        // Load the HMM model from a file
        try {
            ObjectOutputStream os = new ObjectOutputStream(
                    new FileOutputStream(new File("HMMModel.ser")));
            loader.loadHMMFromFile("HMMModel.txt");
            os.writeObject(loader.hmm);
            os.close();
        } catch (Exception ex) {
            System.err.println("ERROR: Failed to write hmm! " + ex);
        }
    }

    public static ArrayList<Vector3d> getRecordsFromFile(File file) {
        ArrayList<Vector3d> recs = new ArrayList<Vector3d>();
        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNext()) {
                String[] nums = sc.nextLine().split(" ");
                Vector3d rec = new Vector3d();
                rec.x = Double.parseDouble(nums[0]);
                rec.y = Double.parseDouble(nums[1]);
                rec.z = Double.parseDouble(nums[2]);
                recs.add(rec);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("Records file not found");
        }
        return recs;
    }
}
