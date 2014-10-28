package com.wizardfight.components;

public class Vector2d {
    public double x;
    public double y;

    Vector2d() {
        x = y = 0;
    }

    Vector2d(double X, double Y) {
        x = X;
        y = Y;
    }

    public double getLength(){
        return Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
    }
    
    @Override
    public String toString() {
        return "x = " + x + "; y = " + y;
    }
}
