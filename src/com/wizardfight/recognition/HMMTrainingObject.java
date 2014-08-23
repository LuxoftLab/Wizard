package com.wizardfight.recognition;

class HMMTrainingObject {

	MatrixDouble alpha; // The forward estimate matrix
	MatrixDouble beta; // The backward estimate matrix
	double[] c; // The scaling coefficient vector
	double pk = 0.0; // P( O | Model )
}
