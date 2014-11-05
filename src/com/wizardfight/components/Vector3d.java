package com.wizardfight.components;

public class Vector3d {
    public final double x;
    public final double y;
	public final double z;

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
