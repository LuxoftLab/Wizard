package com.wizardfight.recognition;

import java.util.ArrayList;

import com.wizardfight.components.Vector3d;

/**
 * The TimeSeriesClassificationData is the main data structure for recording,
 * labeling, managing, saving, and loading training data for supervised temporal
 * learning problems. TimeSeriesClassificationData sample will consist of an N
 * dimensional time series of length M. The length of each time series sample
 * (i.e. M) can be different for each datum in the dataset.
 */
class TimeSeriesClassificationData {

    private int numDimensions = 0; // /< The number of dimensions in the dataset

    // ArrayList containing a set of externalRanges set by the user
    private final ArrayList<ClassTracker> classTracker = new ArrayList<ClassTracker>();
    // ArrayList of ClassTracker, which keeps track of the number of
    // samples of each class
    private TimeSeriesClassificationSample data = new TimeSeriesClassificationSample();

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
        if (classLabel == 0) {
            System.err
                    .println("addSample(int classLabel, MatrixDouble sample) - the class label can not be 0!");
            return false;
        }

        data = new TimeSeriesClassificationSample(classLabel, trainingSample);

        if (classTracker.isEmpty()) {
            ClassTracker tracker = new ClassTracker(classLabel, 1);
            classTracker.add(tracker);
        } else {
            boolean labelFound = false;
            for (ClassTracker aClassTracker : classTracker) {
                if (classLabel == aClassTracker.classLabel) {
                    aClassTracker.counter++;
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
    void clear() {
        data = null;
    }

    public void loadDatasetFromRecords(ArrayList<Vector3d> records) {
        int numClasses = 1;
        numDimensions = 3;
        clear();
        // Resize the class counter buffer and load the counters
        classTracker.ensureCapacity(numClasses);
        classTracker.add(new ClassTracker(-1, 1));

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
    }
}
