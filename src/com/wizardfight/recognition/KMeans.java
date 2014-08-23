package com.wizardfight.recognition;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class implements the KMeans clustering algorithm.
 */
class KMeans implements Serializable {

    private static final long serialVersionUID = 3L;

    protected boolean trained = false;
    protected boolean computeTheta = true;
    protected int numClusters = 10;
    protected int minNumEpochs = 5;
    protected int maxNumEpochs = 1000;
    protected int numTrainingSamples = 0; // /<Number of training examples
    protected int nchg = 0; // /<Number of values changes
    protected double finalTheta = 0;
    protected double minChange = 1.0e-5;
    protected MatrixDouble clusters = new MatrixDouble();

    protected ArrayList<Double> thetaTracker = new ArrayList<Double>();
    protected ArrayList<MinMax> ranges = new ArrayList<MinMax>();

    protected int[] assign; // = new ArrayList<Integer>();
    protected int[] count; // = new ArrayList<Integer>();

    protected boolean useScaling;
    protected boolean converged;
    protected int numInputDimensions;
    protected int numTrainingIterationsToConverge;

    MatrixDouble getClusters() {
        return clusters;
    }

    public void setComputeTheta(boolean computeTheta) {
        this.computeTheta = computeTheta;
    }

    public void setNumClusters(int numClusters) {
        this.numClusters = numClusters;
    }

    public void setMinNumEpochs(int minNumEpochs) {
        this.minNumEpochs = minNumEpochs;
    }

    public void setMaxNumEpochs(int maxNumEpochs) {
        this.maxNumEpochs = maxNumEpochs;
    }

    public void setMinChange(double minChange) {
        this.minChange = minChange;
    }
}
