package com.wizardfight.recognition;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class acts as the main interface for using a Hidden Markov Model.
 */
public class HMM implements Serializable {

    private static final long serialVersionUID = 2L;
    // Variables for all the HMMs
    boolean trained = false;
    boolean useScaling = false;
    boolean useNullRejection = false;

    int delta = 1; // The number of states a model can move to in a
    // HMMModelTipes.LEFTRIGHT model
    protected int maxNumIter = 100; // The maximum number of iter allowed during
    // the full training
    int numClasses;
    int numInputDimensions = 0;
    int numRandomTrainingIterations;
    int numStates = 5; // The number of states for each model
    int numSymbols = 10; // The number of symbols for each model
    private int predictedClassLabel;

    double bestDistance;
    double maxLikelihood;
    protected double minImprovement = 1.0e-2; // The minimum improvement value
    // for each model during training
    int[] classLabels = new int[0];
    double[] classLikelihoods = new double[0];
    double[] classDistances = new double[0];
    double[] nullRejectionThresholds;

    HMMModelTypes modelType = HMMModelTypes.LEFTRIGHT;

    final ArrayList<HiddenMarkovModel> models = new ArrayList<HiddenMarkovModel>();

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
    }

    private double antilog(double d) {
        return Math.exp(d);
    }

    void clear() {
        models.clear();
    }
}
