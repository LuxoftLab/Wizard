package com.wizardfight.recognition;

/**
 * This class stores the timeseries data for a single labelled timeseries
 * classification sample.
 */
public class TimeSeriesClassificationSample {

	protected int classLabel;
	protected MatrixDouble data;

	public TimeSeriesClassificationSample() {
		classLabel = 0;
		data = null;
	}

	public TimeSeriesClassificationSample(int classLabel, MatrixDouble data) {
		this.classLabel = classLabel;
		this.data = data;
	}

	public double getData(int i, int j) {
		return data.dataPtr[i][j];
	}

	public int getLength() {
		return data.rows;
	}

	public int getClassLabel() {
		return classLabel;
	}

	public final MatrixDouble getData() {
		return data;
	}

	public void setTrainingSample(int classLabel, MatrixDouble data) {
		this.classLabel = classLabel;
		this.data = data;
	}
}
