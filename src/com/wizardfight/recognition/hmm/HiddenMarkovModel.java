package com.wizardfight.recognition.hmm;

import java.io.Serializable;

/**
 * This class implements a discrete Hidden Markov Model.
 */
class HiddenMarkovModel implements Serializable {

	private static final long serialVersionUID = 1L;
	private int numStates = 0; // The number of states for this model
	private int[] estimatedStates = new int[0];

	private double[] pi; // The state start probability vector

	double[][] a; // The transitions probability matrix
	double[][] b; // The emissions probability matrix

	double predict(int[] obs) {
		final int N = numStates;
		final int T = obs.length;
		int t, i, j;
		double[][] alpha = new double[ T ][ numStates ];
		double[] c = new double[T];

		// //////////////// Run the forward algorithm ////////////////////////
		// Step 1: Init at t=0
		t = 0;
		c[t] = 0.0;
		for (i = 0; i < N; i++) {
			double val = pi[ i ] * b[ i ][ obs[t] ];
			alpha[ t ][ i ] = val;
			c[ t ] += val;
		}

		// Set the inital scaling coeff
		c[ t ] = 1.0 / c[ t ];

		// Scale alpha
		for (i = 0; i < N; i++) {
			alpha[ t ][ i ] *= c[ t ];
		}

		// Step 2: Induction
		for (t = 1; t < T; t++) {
			c[ t ] = 0.0;
			for (j = 0; j < N; j++) {
				alpha[ t ][ j ] = 0.0;
				for (i = 0; i < N; i++) {
					alpha[ t ][ j ] += alpha[ t - 1 ][ i ] * a[ i ][ j ];
				}

				alpha[ t ][ j ] *= b[ j ][ obs[t] ];

				c[ t ] += alpha[ t ][ j ];
			}

			// Set the scaling coeff
			c[ t ] = 1.0 / c[ t ];

			// Scale Alpha
			for (j = 0; j < N; j++) {
				alpha[ t ][ j ] *= c[ t ];
			}
		}

		if (estimatedStates.length != T) {
			estimatedStates = new int[ T ];
		}
		for (t = 0; t < T; t++) {
			double maxValue = 0;
			for (i = 0; i < N; i++) {
				if (alpha[ t ][ i ] > maxValue) {
					maxValue = alpha[ t ][ i ];
					estimatedStates[ t ] = i;
				}
			}
		}

		// Termination
		double loglikelihood = 0.0;
		for (t = 0; t < T; t++) {
			loglikelihood += Math.log( c[ t ] );
		}
		return -loglikelihood; // Return the negative log likelihood
	}
}
