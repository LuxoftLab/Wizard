package com.example.wizard1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.example.wizard1.components.Vector3d;
import com.example.wizard1.components.Vector4d;

public class Cascade {
	
	ArrayList<Vector3d> val;
	ArrayList<Integer> count;
	
	int T = 20;
	int MS = 1000000;
	
	public Cascade() {
		val = new ArrayList<Vector3d>();
		count = new ArrayList<Integer>();
	}
	
	public void train(ArrayList<Vector4d> data) {
		long first = data.get(0).t;
		int i = 0;
		for(; i<data.size(); i++) {
			Vector4d t = data.get(i);
			int time = (int) ((t.t-first)/MS/T);
			if(time >= val.size()) {
				for(int j=val.size(); j<=time; j++) {
					val.add(new Vector3d(0, 0, 0));
					count.add(0);
				}
			}
			int n = count.get(time);
			Vector3d v = val.get(time);
			v.x = (v.x*n+t.x)/(n+1);
			v.y = (v.y*n+t.y)/(n+1);
			v.z = (v.z*n+t.z)/(n+1);
			count.set(time, n+1);
		}
	}
	
	public double test(ArrayList<Vector4d> data) {
		int i = 0;
		long first = data.get(0).t;
		double X=0, Y=0, Z=0;
		for(; i<data.size(); i++) {
			Vector4d t = data.get(i);
			int time = (int) ((t.t-first)/MS/T);
			Vector3d v;
			if(time >= val.size()) {
				break;
			} else {
				v = val.get(time);
			}
			X += (v.x-t.x)*(v.x-t.x);
			Y += (v.y-t.y)*(v.y-t.y);
			Z += (v.z-t.z)*(v.z-t.z);
		}
		double f;
		long dl = (data.get(data.size()-1).t - data.get(0).t)/MS;
		long vl = (val.size()-1)*T;
		if(dl>vl) {
			f = (double)dl/vl;
		} else {
			f = (double)vl/dl;
		}
		X = Math.sqrt(X/data.size())*f;
		Y = Math.sqrt(Y/data.size())*f;
		Z = Math.sqrt(Z/data.size())*f;
		double max = X > Y ? X : Y;
		max = max > Z ? max : Z;
		return max;
		//return X+Y+Z;
	}
	
	public void save(DataOutputStream out) throws IOException {
		out.writeInt(val.size());
		for(Vector3d v : val) {
			out.writeDouble(v.x);
			out.writeDouble(v.y);
			out.writeDouble(v.z);
		}
		for(Integer i : count) {
			out.writeInt(i);
		}
		out.close();
	}
	
	public void load(DataInputStream in) throws IOException {
		int size = in.readInt();
		for(int i=0; i<size; i++) {
			val.add(new Vector3d(in.readDouble(), in.readDouble(), in.readDouble()));
		}
		for(int i=0; i<size; i++) {
			count.add(in.readInt());
		}
		in.close();
	}
}
