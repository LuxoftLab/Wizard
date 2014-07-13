package com.example.wizard1;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;




import java.util.Map.Entry;

import com.example.wizard1.components.Vector3d;
import com.example.wizard1.components.Vector4d;

import android.content.res.Resources;
import android.util.Log;

public class Recognition {
	static HashMap<Shape, Cascade> cascades = new HashMap<Shape, Cascade>();

	public static void putCascade (Resources res, int rawId, Shape shape) 
		throws IOException {
		Cascade c = new Cascade();
		c.load(new DataInputStream(res.openRawResource(rawId)));
		cascades.put(shape, c);
	}
	
	public static void init(Resources res) throws IOException {
		putCascade(res, R.raw.triangle, Shape.TRIANGLE);	
		putCascade(res, R.raw.circle, Shape.CIRCLE);
		putCascade(res, R.raw.clock, Shape.CLOCK);
		putCascade(res, R.raw.z, Shape.Z);
		putCascade(res, R.raw.v, Shape.V);
		putCascade(res, R.raw.pi, Shape.PI);
		putCascade(res, R.raw.shield, Shape.SHIELD);
	}

	public static Shape recognize(ArrayList<Vector4d> data) {
		double min = Double.MAX_VALUE;
    	Shape s = Shape.FAIL;
    	Collection<Entry<Shape, Cascade>> cs = cascades.entrySet();
    	for(Entry<Shape, Cascade> e : cs) {
    		double r = e.getValue().test(data);
    		Log.d("recognition", r+"");
    		if(r < min) {
    			min = r;
    			s = e.getKey();
    		}
    	}
    	return s;
	}
}