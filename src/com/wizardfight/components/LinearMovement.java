package com.wizardfight.components;

public class LinearMovement {
    protected double v;
    protected double s;
    protected double a;

    public LinearMovement() {
        v = s = a = 0;
    }

    public double getS() {
        return s;
    }

    public void calcNextCoord(double ai, double dt) {
        a = ai;
        s += v * dt + a * dt * dt / 2;
        v += a * dt;
    }
}
