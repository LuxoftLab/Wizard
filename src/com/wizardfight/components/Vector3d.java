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

    public void add(Vector3d v1) {
        x += v1.x;
        y += v1.y;
        z += v1.z;
    }

    @Override
    public double getLength(){
        return Math.sqrt(x*x + y*y + z*z);
    }

    @Override
    public String toString() {
        return super.toString() + "; z = " + z;
    }
}
