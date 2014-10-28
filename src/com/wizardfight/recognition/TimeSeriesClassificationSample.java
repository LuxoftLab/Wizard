package com.wizardfight.recognition;

/**
 * This class stores the timeseries data for a single labelled timeseries
 * classification sample.
 */
public class TimeSeriesClassificationSample {

	private int classLabel;
	private MatrixDouble data;

	public TimeSeriesClassificationSample() {
		classLabel = 0;
		data = null;
	}

	public TimeSeriesClassificationSample(int classLabel, MatrixDouble data) {
		this.classLabel = classLabel;
		this.data = data;
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
