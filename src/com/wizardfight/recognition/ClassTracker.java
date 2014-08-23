package com.wizardfight.recognition;

public class ClassTracker {

    public int classLabel = 0;
    public int counter = 0;
    public String className = "NOT_SET";

    public ClassTracker(int classLabel, int counter, String className) {
        this.classLabel = classLabel;
        this.counter = counter;
        this.className = className;
    }

    public ClassTracker(int classLabel, int i) {
        this.classLabel = classLabel;
        this.counter = i;
    }
}
