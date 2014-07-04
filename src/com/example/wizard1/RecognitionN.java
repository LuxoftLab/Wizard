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

public class RecognitionN {
	
	static HashMap<Shape, Cascade> cascades = new HashMap<Shape, Cascade>();
	
	public static void init(Resources res) throws IOException {
		Cascade c = new Cascade();
		c.load(new DataInputStream(res.openRawResource(R.raw.circle)));
		cascades.put(Shape.CIRCLE, c);
		c = new Cascade();
		c.load(new DataInputStream(res.openRawResource(R.raw.rect)));
		cascades.put(Shape.SQUARE, c);
		c = new Cascade();
		c.load(new DataInputStream(res.openRawResource(R.raw.triangle)));
		cascades.put(Shape.TRIANGLE, c);
		c = new Cascade();
		c.load(new DataInputStream(res.openRawResource(R.raw.clock)));
		cascades.put(Shape.CLOCK, c);
		c = new Cascade();
		c.load(new DataInputStream(res.openRawResource(R.raw.z)));
		cascades.put(Shape.FAIL, c);
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
