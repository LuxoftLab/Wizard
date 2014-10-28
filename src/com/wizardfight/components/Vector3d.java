package com.wizardfight.components;

public class Vector3d extends Vector2d {
	public double z;

    public Vector3d() {
        super();
        z = 0;
    }

    public Vector3d(double X, double Y, double Z) {
        super(X, Y);
        z = Z;
    }

    @Override
    public String toString() {
        return super.toString() + "; z = " + z;
    }
}
