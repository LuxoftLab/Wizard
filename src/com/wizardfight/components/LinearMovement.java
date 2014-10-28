package com.wizardfight.components;

class LinearMovement {
    private double v;
    private double s;
    private double a;

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
