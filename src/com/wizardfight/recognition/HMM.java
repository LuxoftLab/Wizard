package com.wizardfight.recognition;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class acts as the main interface for using a Hidden Markov Model.
 */
public class HMM implements Serializable {

    private static final long serialVersionUID = 2L;
    // Variables for all the HMMs
    private boolean useNullRejection = false; // would be useful if failed gesture detection

    private int numClasses;
    private int predictedClassLabel;

    private double bestDistance; 
    private double maxLikelihood; 
    // for each model during training
    private int[] classLabels = new int[0];
    private double[] classLikelihoods = new double[0];
    private double[] classDistances = new double[0];
    private double[] nullRejectionThresholds;


    private final ArrayList<HiddenMarkovModel> models = new ArrayList<HiddenMarkovModel>();

    public int getPredictedClassLabel() {
    	return predictedClassLabel;
    }

    public void predict(int[] timeseries) {
        final int M = timeseries.length;
        int[] observationSequence = new int[M];

        System.arraycopy(timeseries, 0, observationSequence, 0, M);
        
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
