package com.wizardfight.recognition.accrecognizer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.wizardfight.components.Vector3d;

public class AccRecognizer implements Serializable {
	private static final long serialVersionUID = 270120151348L;
	private static final double WORTH_DENSITY = 0.05;
	
	public enum Speed {
		FAST(35),
		MID(50),
		SLOW(80);

		public int size;
		private Speed(int size) {
			this.size = size;
		}
	}
	
	private HashMap<Speed, SpeedModel> sModels;
	private double lastDensity;
	private double lastAccuracy;
	
	public AccRecognizer() {
		sModels = new HashMap<Speed, SpeedModel>();
	}
	
	public void train(File folder) {
		File[] smFiles = folder.listFiles();
		int n = smFiles.length;
		
		for(int i=0; i<n; i++) {
			try {
				String fileName = smFiles[i].getName();
				Speed speed = Speed.valueOf(fileName.toUpperCase());
				SpeedModel speedModel = new SpeedModel(speed);
				speedModel.initFromFolder(smFiles[i]);
				System.out.println("**************\n\nput speed model: " + speed.name());
				sModels.put(speed, speedModel);
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("wrong folder : " + smFiles[i].getName());
			}
		}
	}
	
	public String recognize(ArrayList<Vector3d> records) {
		Speed[] speeds = Speed.values();
		int bestSizeDiff = Integer.MAX_VALUE;
		Speed nearestSpeed = null;
		
		for(Speed s : speeds) {
			int diff = s.size - records.size();
			if(diff < 0) diff = -diff;
			if(diff < bestSizeDiff) {
				bestSizeDiff = diff;
				nearestSpeed = s;
			}
		}
		
		String shapeName = sModels.get(nearestSpeed).recognize(records);
		
		return shapeName;
	}
	
	public void print() {
		System.out.println("***\n***\nRECOGNIZER\n***\n***\n");
		for (Map.Entry<Speed, SpeedModel> entry : sModels.entrySet()){
			entry.getValue().print();
		}
	}
	
	public double getDensity() {
		return lastDensity;
	}
	
	public double getAccuracy() {
		return lastAccuracy;
	}

	public boolean isGoodDensity() {
		return lastDensity <= WORTH_DENSITY;
	}
}
