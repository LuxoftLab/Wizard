package com.wizardfight.accrecognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.wizardfight.accrecognizer.AccRecognizer.Speed;
import com.wizardfight.components.Vector3d;

public class SpeedModel implements Serializable {
	private static final long serialVersionUID = 270120151349L;
	
	private final Speed modelSpeed;
	private HashMap<String, ArrayList<Vector3d>> shapeRecords;
	private double lastDensity;
	private double lastAccuracy;

	public SpeedModel(Speed speed) {
		modelSpeed = speed;
		shapeRecords = new HashMap<String, ArrayList<Vector3d>>();
	}

	public void initFromFolder(File folder) throws FileNotFoundException {
		File[] fShapes = folder.listFiles();
		for (int i = 0; i < fShapes.length; i++) {
			assert (fShapes[i].isDirectory());
			String shapeName = fShapes[i].getName();
			ArrayList<Vector3d> avgRecords = getAvgRecords(fShapes[i]);
			System.out.println("PUT SHAPE " + shapeName);
			for (Vector3d row : avgRecords) {
				System.out.println(row);
			}
			shapeRecords.put(shapeName, avgRecords);
		}
	}

	private ArrayList<Vector3d> getAvgRecords(File folder)
			throws FileNotFoundException {
		ArrayList<Vector3d> avgRecords = new ArrayList<Vector3d>();
		for(int i=0; i<modelSpeed.size; i++) {
			avgRecords.add(new Vector3d(0.0, 0.0, 0.0));
		}
		
		File[] fRecords = folder.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".txt");
		    }
		});
		
		for (int i = 0; i < fRecords.length; i++) {
			System.out.println(">>read file " + fRecords[i].getName());
			ArrayList<Vector3d> records = getRecordsFromFile(fRecords[i]);
			records = Vector3d.squeeze(records, modelSpeed.size); //TODO check this scale
			for(int j=0; j<avgRecords.size(); j++) {
				Vector3d v = avgRecords.get(j);
				Vector3d v2 = records.get(j);
				v.x += v2.x; 
				v.y += v2.y; 
				v.z += v2.z;
			}
		}
		
		for(Vector3d v : avgRecords) {
			v.x /= fRecords.length;
			v.y /= fRecords.length;
			v.z /= fRecords.length;
		}
		
		return avgRecords;
	}
	
	public static ArrayList<Vector3d> getRecordsFromFile(File f) {
		ArrayList<Vector3d> records = new ArrayList<Vector3d>();
		try {
			// read input data from file
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String s;
			while ((s = reader.readLine()) != null) {
				String[] tokens = s.trim().split("\\s++");
				double[] v = new double[3];
				for (int i = 0; i < 3; i++) {
					v[i] = Double.parseDouble(tokens[i]);
				}
				Vector3d record = new Vector3d(v[0], v[1], v[2]);
				records.add(record);
			}
			reader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return records;
	}
	
	public String recognize(ArrayList<Vector3d> records) {
		double bestLikelihood = Double.MAX_VALUE;
		double[] likelihoods = new double[ shapeRecords.size() ];
		double likelihood;
		String bestShape = "none";
		int c = 0;
		records = Vector3d.resize(records, modelSpeed.size);
		for (Map.Entry<String, ArrayList<Vector3d>> entry 
				: shapeRecords.entrySet()){
			likelihood = 0.0;
			ArrayList<Vector3d> modelData = entry.getValue();
			
			for(int i=0; i<modelSpeed.size; i++) {
				Vector3d vm = modelData.get(i);
				Vector3d v = records.get(i);
				double diff = Math.sqrt(
						(vm.x - v.x) * (vm.x - v.x) +
						(vm.y - v.y) * (vm.y - v.y) +
						(vm.z - v.z) * (vm.z - v.z) );
				likelihood += diff;
			}
			
			likelihoods[c++] = likelihood;
			
			if(likelihood < bestLikelihood) {
				bestLikelihood = likelihood;
				bestShape = entry.getKey();
			}
		}
		
		double sum = 0.0;

		for(double d : likelihoods) {
			sum += d;
		}

		lastDensity = bestLikelihood / sum;
		lastAccuracy = bestLikelihood;
		
		return bestShape;
	}
	
	public double getDensity() {
		return lastDensity;
	}
	
	public double getAccuracy() {
		return lastAccuracy;
	}
	
	public void print() {
		System.out.println("\n" + modelSpeed.name() + "\n");
		for (Map.Entry<String, ArrayList<Vector3d>> entry 
				: shapeRecords.entrySet()){
			System.out.println(modelSpeed.name() + " " + entry.getKey());
			for (Vector3d row : entry.getValue()) {
				System.out.println(row);
			}
		}
	}
}
