package com.wizardfight.recognition;

class ClassTracker {

    public int classLabel = 0;
    public int counter = 0;

    public ClassTracker(int classLabel, int counter, String className) {
        this.classLabel = classLabel;
        this.counter = counter;
    }

    public ClassTracker(int classLabel, int i) {
        this.classLabel = classLabel;
        this.counter = i;
    }
}
