package com.wizardfight.components;

public class Vector4d extends Vector3d {
	public long t;

    public Vector4d() {
        super();
        t = 0;
    }

    public Vector4d(double X, double Y, double Z, long T) {
        super(X, Y, Z);
        t = T;
    }

    public long getT() {
        return t;
    }

    @Override
    public String toString() {
        return super.toString() + "; t = " + t;
    }
}
