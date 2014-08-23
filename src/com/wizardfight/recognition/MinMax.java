package com.wizardfight.recognition;

public class MinMax {

    public MinMax() {
        minValue = 0;
        maxValue = 0;
    }

    public MinMax(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public boolean updateMinMax(double newValue) {
        if (newValue < minValue) {
            minValue = newValue;
            return true;
        }
        if (newValue > maxValue) {
            maxValue = newValue;
            return true;
        }
        return false;
    }

    public double minValue;
    public double maxValue;
}
