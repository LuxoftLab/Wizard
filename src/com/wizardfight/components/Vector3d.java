package com.wizardfight.components;

public class Vector3d {
    public double x;
    public double y;
	public double z;

    public Vector3d() {
        x = y = z = 0;
    }

    public Vector3d(double X, double Y, double Z) {
        x = X;
        y = Y;
        z = Z;
    }

    @Override
    public String toString() {
        return "x = " + x + "; y = " + y + " z = " + z;
    }
}
