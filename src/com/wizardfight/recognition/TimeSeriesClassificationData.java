package com.wizardfight.recognition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.wizardfight.components.Vector3d;

/**
 * The TimeSeriesClassificationData is the main data structure for recording,
 * labeling, managing, saving, and loading training data for supervised temporal
 * learning problems. TimeSeriesClassificationData sample will consist of an N
 * dimensional time series of length M. The length of each time series sample
 * (i.e. M) can be different for each datum in the dataset.
 */
public class TimeSeriesClassificationData {

    int numDimensions = 0; // /< The number of dimensions in the dataset

    int kFoldValue; // /< The number of folds the dataset has been spilt into
    // for cross valiation
    boolean crossValidationSetup = false; // /< A flag to show if the dataset is
    // ready for cross validation
    boolean useExternalRanges = false; // /< A flag to show if the dataset
    // should be scaled using the
    // externalRanges values
    boolean allowNullGestureClass = false; // /< A flag that enables/disables a
    // user from adding new samples with
    // a class label matching the
    // default null gesture label

    ArrayList<MinMax> externalRanges = new ArrayList<MinMax>();
    // ArrayList containing a set of externalRanges set by the user
    ArrayList<ClassTracker> classTracker = new ArrayList<ClassTracker>();
    // ArrayList of ClassTracker, which keeps track of the number of
    // samples of each class
    TimeSeriesClassificationSample data = new TimeSeriesClassificationSample();

    /**
     * Constructor, sets the number of dimensions of the training data. The name
     * of the dataset should not contain any spaces.
     *
     * @param int numDimensions: the number of dimensions of the training data,
     * should be an unsigned integer greater than 0
     */
    public TimeSeriesClassificationData(int numDimensions) {
        this.numDimensions = numDimensions;
    }

    public TimeSeriesClassificationData() {
    }

    /**
     * Array Subscript Operator, returns the TimeSeriesClassificationSample at
     * index i. It is up to the user to ensure that i is within the range of [0
     * totalNumSamples-1]
     *
     * @param int i: the index of the training sample you want to access. Must
     * be within the range of [0 totalNumSamples-1]
     * @return a reference to the i'th TimeSeriesClassificationSample
     */
    public TimeSeriesClassificationSample getData() {
        return data;
    }

    public boolean hasSample() {
        return (data == null);
    }

    /**
     * Sets the number of dimensions in the training data. This should be an
     * unsigned integer greater than zero. This will clear any previous training
     * data and counters. This function needs to be called before any new
     * samples can be added to the dataset, unless the numDimensions variable
     * was set in the constructor or some data was already loaded from a file
     *
     * @param const UINT numDimensions: the number of dimensions of the training
     * data. Must be an unsigned integer greater than zero
     * @return true if the number of dimensions was correctly updated, false
     * otherwise
     */
    public boolean setNumDimensions(final int numDimensions) {
        if (numDimensions > 0) {
            // Clear any previous training data
            clear();

            // Set the dimensionality of the training data
            this.numDimensions = numDimensions;

            useExternalRanges = false;
            externalRanges.clear();

            return true;
        }

        System.err
                .println("setNumDimensions(int numDimensions) - The number of dimensions of the dataset must be greater than zero!");
        return false;
    }

    /**
     * Adds a new labelled timeseries sample to the dataset. The dimensionality
     * of the sample should match the number of dimensions in the dataset. The
     * class label should be greater than zero (as zero is used as the default
     * null rejection class label).
     *
     * @param int classLabel: the class label of the corresponding sample
     * @param MatrixDouble trainingSample: the new sample you want to add to the
     * dataset. The dimensionality of this sample (i.e. Matrix columns) should
     * match the number of dimensions in the dataset, the rows of the Matrix
     * represent time and do not have to be any specific length
     * @return true if the sample was correctly added to the dataset, false
     * otherwise
     */
    public boolean addSample(int classLabel, MatrixDouble trainingSample) {
        if (trainingSample.cols != numDimensions) {
            System.err
                    .println("addSample(int classLabel, MatrixDouble trainingSample) - The dimensionality of the training sample ("
                            + trainingSample.cols
                            + ") does not match that of the dataset ("
                            + numDimensions + ")");
            return false;
        }

        // The class label must be greater than zero (as zero is used for the
        // null rejection class label
        if (classLabel == 0 && !allowNullGestureClass) {
            System.err
                    .println("addSample(int classLabel, MatrixDouble sample) - the class label can not be 0!");
            return false;
        }

        TimeSeriesClassificationSample newSample = new TimeSeriesClassificationSample(
                classLabel, trainingSample);
        data = newSample;

        if (classTracker.isEmpty()) {
            ClassTracker tracker = new ClassTracker(classLabel, 1);
            classTracker.add(tracker);
        } else {
            boolean labelFound = false;
            for (int i = 0; i < classTracker.size(); i++) {
                if (classLabel == classTracker.get(i).classLabel) {
                    classTracker.get(i).counter++;
                    labelFound = true;
                    break;
                }
            }
            if (!labelFound) {
                ClassTracker tracker = new ClassTracker(classLabel, 1);
                classTracker.add(tracker);
            }
        }
        return true;
    }

    /**
     * Clears any previous training data and counters
     */
    public void clear() {
        data = null;
    }

    public boolean loadDatasetFromRecords(ArrayList<Vector3d> records) {
        int numClasses = 1;
        numDimensions = 3;
        clear();
        // Resize the class counter buffer and load the counters
        classTracker.ensureCapacity(numClasses);
        classTracker.add(new ClassTracker(-1, 1, "NOT_SET"));

        int classLabel = -1;
        int timeSeriesLength = records.size();

        MatrixDouble trainingExample = new MatrixDouble(timeSeriesLength,
                numDimensions);

        for (int i = 0; i < timeSeriesLength; i++) {
            trainingExample.dataPtr[i][0] = records.get(i).x;
            trainingExample.dataPtr[i][1] = records.get(i).y;
            trainingExample.dataPtr[i][2] = records.get(i).z;
        }

        data = new TimeSeriesClassificationSample();
        data.setTrainingSample(classLabel, trainingExample);

        return true;
    }
}
