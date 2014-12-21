package com.wizardfight.recognition;

import java.util.ArrayList;
import java.io.Serializable;

/**
 * This class implements a discrete Hidden Markov Model.
 */
class HiddenMarkovModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean modelTrained = false; // TODO delete

    private int numStates = 0; // The number of states for this model
    private int numSymbols = 0; // TODO delete
    private int delta = 1;   // TODO delete

    private int numRandomTrainingIterations = 5; // TODO delete
    private int maxNumIter = 100; // TODO delete

    private double logLikelihood = 0.0; // TODO delete

    private double cThreshold = -1000; // TODO delete
    private double minImprovement = 1.0e-5; // TODO delete


    private int[] observationSequence = new int[0]; // TODO delete
    private int[] estimatedStates = new int[0];

    private double[] pi; // The state start probability vector

    private HMMModelTypes modelType = HMMModelTypes.ERGODIC; // TODO delete

    private ArrayList<Double> trainingIterationLog = new ArrayList<Double>();  // TODO delete
   
    private final MatrixDouble a = new MatrixDouble(); // The transitions probability matrix
    private final MatrixDouble b = new MatrixDouble(); // The emissions probability matrix

    double predict(int[] obs) {
        final int N = numStates;
        final int T = obs.length;
        int t, i, j;
        MatrixDouble alpha = new MatrixDouble(T, numStates);
        double[] c = new double[T];

	// //////////////// Run the forward algorithm ////////////////////////
        // Step 1: Init at t=0
        t = 0;
        c[t] = 0.0;
        for (i = 0; i < N; i++) {
            double val = pi[i] * b.dataPtr[i][obs[t]];
            alpha.dataPtr[t][i] = val;
            c[t] += val;
        }

        // Set the inital scaling coeff
        c[t] = 1.0 / c[t];

        // Scale alpha
        for (i = 0; i < N; i++) {
            double val = alpha.dataPtr[t][i];
            val *= c[t];
            alpha.dataPtr[t][i] = val;
        }

        // Step 2: Induction
        for (t = 1; t < T; t++) {
            c[t] = 0.0;
            for (j = 0; j < N; j++) {
                alpha.dataPtr[t][j] = 0.0;
                for (i = 0; i < N; i++) {
                    double val = alpha.dataPtr[t][j];
                    val += alpha.dataPtr[t - 1][i] * a.dataPtr[i][j];
                    alpha.dataPtr[t][j] = val;

                }
                double val = alpha.dataPtr[t][j];
                val *= b.dataPtr[j][obs[t]];
                alpha.dataPtr[t][j] = val;
                c[t] += alpha.dataPtr[t][j];
            }

            // Set the scaling coeff
            c[t] = 1.0 / c[t];

            // Scale Alpha
            for (j = 0; j < N; j++) {
                double val = alpha.dataPtr[t][j];
                val *= c[t];
                alpha.dataPtr[t][j] = val;
            }
        }

        if (estimatedStates.length != T) {
            estimatedStates = new int[T];
        }
        for (t = 0; t < T; t++) {
            double maxValue = 0;
            for (i = 0; i < N; i++) {
                if (alpha.dataPtr[t][i] > maxValue) {
                    maxValue = alpha.dataPtr[t][i];
                    estimatedStates[t] = i;
                }
            }
        }

        // Termination
        double loglikelihood = 0.0;
        for (t = 0; t < T; t++) {
            loglikelihood += Math.log(c[t]);
        }
        return -loglikelihood; // Return the negative log likelihood
    }
}
