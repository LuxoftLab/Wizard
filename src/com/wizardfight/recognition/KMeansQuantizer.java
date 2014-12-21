package com.wizardfight.recognition;

import java.io.Serializable;
import java.util.ArrayList;

public class KMeansQuantizer implements Serializable {

    private static final long serialVersionUID = 4L;

    private int numClusters; 
    private int numInputDimensions = 0; //TODO set as constant after changing serizalization
    private final MatrixDouble clusters = new MatrixDouble();
    private final ArrayList<Double> featureVector = new ArrayList<Double>();
    private ArrayList<Double> quantizationDistances = new ArrayList<Double>();

    /**
     * Default constructor. Initalizes the KMeansQuantizer, setting the number
     * of input dimensions and the number of clusters to use in the quantization
     * model.
     *
     * @param int numClusters: the number of quantization clusters
     */
    public KMeansQuantizer(final int numClusters) {
        this.numClusters = numClusters;
        featureVector.add(0.0);
    }

    public int quantize(double[] inputVector) {
        // Find the minimum cluster
        double minDist = Double.MAX_VALUE;
        int quantizedValue = 0;

        for (int k = 0; k < numClusters; k++) {
            // Compute the squared Euclidean distance
            quantizationDistances.add(k, 0.0);
            for (int i = 0; i < numInputDimensions; i++) {
                double val = quantizationDistances.get(k);
                val += Math.pow(inputVector[i] - clusters.dataPtr[k][i], 2);
                quantizationDistances.set(k, val);
            }

            if (quantizationDistances.get(k) < minDist) {
                minDist = quantizationDistances.get(k);
                quantizedValue = k;
            }
        }
        featureVector.set(0, (double) quantizedValue);

        return quantizedValue;
    }
    
    public void refresh() {
        quantizationDistances = new ArrayList<Double>();
    }

    public ArrayList<Double> getFeatureVector() {
        return featureVector;
    }
}
