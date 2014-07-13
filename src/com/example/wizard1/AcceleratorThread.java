package com.example.wizard1;

import java.util.ArrayList;
import com.example.wizard1.components.Vector4d;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AcceleratorThread extends Thread implements SensorEventListener {
	private boolean listening;
//	private boolean configured;
	private long prevTimeStamp;
	private Looper mLooper;
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	private ArrayList <Vector4d> records;
	private double calib;

	public AcceleratorThread(SensorManager sm, Sensor s, double c) {
		this.calib = c;
		setName("Accelerator thread");
		prevTimeStamp = 0;
		mSensorManager = sm;
		mAccelerometer = s;
		listening = false;
//		configured = false;
	}
	
	public void run() {
		Looper.prepare();
		Handler handler = new Handler();
		mLooper = Looper.myLooper();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME, handler);
		Looper.loop();
//		configured = true;
	}
	
	public void startGettingData() {
		records = new ArrayList<Vector4d>();
		listening = true;
	}
	
	public ArrayList<Vector4d> stopAndGetResult() {
		listening = false;
		return records;
	}
	
	public void stopLoop() {
		mSensorManager.unregisterListener(this);
		if( mLooper != null ) mLooper.quit();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	 public void onSensorChanged(SensorEvent event) {
		if( !listening ) return;
//    	long timeStamp = event.timestamp / 1000000;
    	long timeStamp = event.timestamp;
    	//at least 20 milliseconds between events
    	if( timeStamp - prevTimeStamp < 20) {
    		//return;
    	}
    	prevTimeStamp = timeStamp;
    	double x = event.values[0];
    	double y = event.values[1];
    	double z = event.values[2];
    	double len = Math.sqrt(x*x+y*y+z*z);
    	Vector4d rec = new Vector4d(
				x-calib*(x/len), y-calib*(y/len), z-calib*(z/len), timeStamp);
		records.add(rec);
    }
}
