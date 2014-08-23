package com.wizardfight.recognition;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class acts as the main interface for using a Hidden Markov Model.
 */
public class HMM implements Serializable {

    private static final long serialVersionUID = 2L;
    // Variables for all the HMMs
    protected boolean trained = false;
    protected boolean useScaling = false;
    protected boolean useNullRejection = false;

    protected int delta = 1; // The number of states a model can move to in a
    // HMMModelTipes.LEFTRIGHT model
    protected int maxNumIter = 100; // The maximum number of iter allowed during
    // the full training
    protected int numClasses;
    protected int numInputDimensions = 0;
    protected int numRandomTrainingIterations;
    protected int numStates = 5; // The number of states for each model
    protected int numSymbols = 10; // The number of symbols for each model
    protected int predictedClassLabel;

    protected double bestDistance;
    protected double maxLikelihood;
    protected double minImprovement = 1.0e-2; // The minimum improvement value
    // for each model during training
    protected int[] classLabels = new int[0];
    protected double[] classLikelihoods = new double[0];
    protected double[] classDistances = new double[0];
    protected double[] nullRejectionThresholds;

    protected HMMModelTypes modelType = HMMModelTypes.LEFTRIGHT;

    protected ArrayList<HiddenMarkovModel> models = new ArrayList<HiddenMarkovModel>();

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
     * @return returns true if the prediction was performed, false otherwise
     */
    public boolean predict(MatrixDouble timeseries) {
        // Covert the matrix double to observations
        final int M = timeseries.rows;
        int[] observationSequence = new int[M];

        for (int i = 0; i < M; i++) {
            observationSequence[i] = (int) timeseries.dataPtr[i][0];
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

        return true;
    }

    private double antilog(double d) {
        return Math.exp(d);
    }

    protected void clear() {
        models.clear();
    }
}
