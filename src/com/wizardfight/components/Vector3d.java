package com.wizardfight.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Vector3d implements Serializable {
	private static final long serialVersionUID = 270120151350L;
    public double x;
    public double y;
	public double z;

    public Vector3d(double X, double Y, double Z) {
        x = X;
        y = Y;
        z = Z;
    }

    @Override
    public String toString() {
        return "x = " + x + "; y = " + y + " z = " + z;
    }
    
    public static ArrayList<Vector3d> resize(ArrayList<Vector3d> a, int size) {
    	ArrayList<Vector3d> s = new ArrayList<Vector3d>();
		double step = a.size() / (double) (size);
		for (int i = 0; i < size; i++) {
			s.add(getArrayResizeItem(a, step * i));
		}
		return s;
    }
    
    public static ArrayList<Vector3d> squeeze(ArrayList<Vector3d> a, int size) {
		if (a.size() < size)
			return a;
		ArrayList<Vector3d> s = new ArrayList<Vector3d>();
		double step = a.size() / (double) (size);
		for (int i = 0; i < size; i++) {
			s.add(getArrayResizeItem(a, step * i));
		}
		return s;
	}

	private static Vector3d getArrayResizeItem(List<Vector3d> a, double i) {
		if (((i == ((int) i))))
			return a.get((int) i);
		if (i + 1 >= a.size())
			return a.get(a.size() - 1);
		double fPart = i % 1;
		double x = a.get((int) i).x + (a.get((int) i + 1).x - a.get((int) i).x)
				* fPart;
		double y = a.get((int) i).y + (a.get((int) i + 1).y - a.get((int) i).y)
				* fPart;
		double z = a.get((int) i).z + (a.get((int) i + 1).z - a.get((int) i).z)
				* fPart;
		return new Vector3d(x, y, z);
	}
}
