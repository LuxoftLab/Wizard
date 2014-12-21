package com.wizardfight.recognition;
import java.io.Serializable;
import java.util.ArrayList;

import android.util.Log;

/**
 * This class acts as the main interface for using a Hidden Markov Model.
 */
public class HMM implements Serializable {

    private static final long serialVersionUID = 2L;
    // Variables for all the HMMs
    private boolean trained = false; //TODO delete after new serialization
    private boolean useScaling = false; //TODO delete after new serialization
    private boolean useNullRejection = false; // would be useful if failed gesture detection

    private int delta = 1; // TODO delete after new serialization
    private int maxNumIter = 100; // TODO delete after new serialization
    private int numClasses;
    private int numInputDimensions = 0;   // TODO delete after new serialization
    private int numRandomTrainingIterations; // TODO delete after new serialization
    private int numStates = 5; // TODO delete after new serialization
    private int numSymbols = 10; // TODO delete after new serialization
    private int predictedClassLabel;

    private double bestDistance; 
    private double maxLikelihood; 
    private double minImprovement = 1.0e-2; // TODO delete after new serialization
    // for each model during training
    private int[] classLabels = new int[0];
    private double[] classLikelihoods = new double[0];
    private double[] classDistances = new double[0];
    private double[] nullRejectionThresholds;

    private HMMModelTypes modelType = HMMModelTypes.LEFTRIGHT; // TODO delete after new serialization

    private final ArrayList<HiddenMarkovModel> models = new ArrayList<HiddenMarkovModel>();

    public int getPredictedClassLabel() {
        if (trained) {
            return predictedClassLabel;
        }
        return 0;
    }

    /**
     * This predicts the class of the timeseries.
     *
     * @param MatrixDouble timeSeries: the input timeseries to classify
     */
    public void predict(MatrixDouble timeseries) {
    	Log.e("Wizard Fight", "Predict [" + timeseries.rows + " ][ " + timeseries.cols + " ]");
        // Covert the matrix double to observations
        final int M = timeseries.rows;
        int[] observationSequence = new int[M];

        for (int i = 0; i < M; i++) {
            observationSequence[i] = (int) timeseries.dataPtr[i][0];
            Log.e("sequence", i + " " + observationSequence[i]);
        }

        if (classLikelihoods.length != numClasses) {
            classLikelihoods = new double[numClasses];
        }
        if (classDistances.length != numClasses) {
            classDistances = new double[numClasses];
        }

        bestDistance = -99e+99;
        int bestIndex = 0;
        double sum = 0;
        for (int k = 0; k < numClasses; k++) {
            classDistances[k] = models.get(k).predict(observationSequence);

            // Set the class likelihood as the antilog of the class distances
            classLikelihoods[k] = antilog(classDistances[k]);

            // The loglikelihood values are negative so we want the values
            // closest to 0
            if (classDistances[k] > bestDistance) {
                bestDistance = classDistances[k];
                bestIndex = k;
            }

            sum += classLikelihoods[k];
        }

        // Turn the class distances into proper likelihoods
        for (int k = 0; k < numClasses; k++) {
            classLikelihoods[k] /= sum;
        }

        maxLikelihood = classLikelihoods[bestIndex];
        predictedClassLabel = classLabels[bestIndex];

        if (useNullRejection) {
            if (maxLikelihood > nullRejectionThresholds[bestIndex]) {
                predictedClassLabel = classLabels[bestIndex];
            } else {
                predictedClassLabel = 0;
            }
        }
    }
    
    public void predict(double[] timeseries) {

        // Covert the matrix double to observations
        final int M = timeseries.length;
        int[] observationSequence = new int[M];

        for (int i = 0; i < M; i++) {
            observationSequence[i] = (int) timeseries[i];
            Log.e("sequence", i + " " + observationSequence[i]);
        }

        if (classLikelihoods.length != numClasses) {
            classLikelihoods = new double[numClasses];
        }
        if (classDistances.length != numClasses) {
            classDistances = new double[numClasses];
        }

        bestDistance = -99e+99;
        int bestIndex = 0;
        double sum = 0;
        for (int k = 0; k < numClasses; k++) {
            classDistances[k] = models.get(k).predict(observationSequence);

            // Set the class likelihood as the antilog of the class distances
            classLikelihoods[k] = antilog(classDistances[k]);

            // The loglikelihood values are negative so we want the values
            // closest to 0
            if (classDistances[k] > bestDistance) {
                bestDistance = classDistances[k];
                bestIndex = k;
            }

            sum += classLikelihoods[k];
        }

        // Turn the class distances into proper likelihoods
        for (int k = 0; k < numClasses; k++) {
            classLikelihoods[k] /= sum;
        }

        maxLikelihood = classLikelihoods[bestIndex];
        predictedClassLabel = classLabels[bestIndex];

        if (useNullRejection) {
            if (maxLikelihood > nullRejectionThresholds[bestIndex]) {
                predictedClassLabel = classLabels[bestIndex];
            } else {
                predictedClassLabel = 0;
            }
        }
    }

    private double antilog(double d) {
        return Math.exp(d);
    }

    void clear() {
        models.clear();
    }
}
