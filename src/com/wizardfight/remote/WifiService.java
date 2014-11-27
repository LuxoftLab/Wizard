package com.wizardfight.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import android.os.Handler;
import android.util.Log;

public class WifiService {
	public static final int NO_ERROR = 0;
	public static final int IO_FAIL = 1;
	public static final int PORT = 8880;
	private static Worker mWorker;
	private static String ip;
	
	public static void init(String addr, Handler handler) {
		close();
		ip = addr;
		Log.d("wifi", "init "+ip);
		mWorker = new Worker(ip, handler);
		mWorker.start();
	}
	
	public static void clearHandler() {
		if(mWorker != null) {
			mWorker.clearHandler();
		}
	}
	
	public static void send(byte[] buffer) {
		if(mWorker != null) {
			mWorker.send(buffer);
		}
	}
	
	public static String getIP() { return ip; }
	
	public static boolean isConnected() {
		if(mWorker == null) return false;
		return mWorker.isWorking();
	}
	
	public static void close() {
		Log.e("WIFI", "WifiService.close");
		if(mWorker != null) {
			mWorker.close();
		}
		Log.e("WIFI", "connected? (after close): " + isConnected());
	}
	
	static class Worker extends Thread {
		private final String mmAddr;
		private Socket mmSocket;
		private Handler mmHandler;
		private final LinkedList<byte[]> mmQueue;
		
		public Worker(String _addr, Handler handler) {
			mmAddr = _addr;
			mmHandler = handler;
			mmQueue = new LinkedList<byte[]>();
		}
		
		public void run() {
			try {
				InetAddress serverAddr = InetAddress.getByName(mmAddr);
				mmSocket = new Socket(serverAddr, PORT);
				mmSocket.setTcpNoDelay(true);
				sendMsgToHandler(NO_ERROR);
				OutputStream out = mmSocket.getOutputStream();
				while(!mmSocket.isClosed()) {
					Log.e("wifi", "thread loop");
					while(!mmQueue.isEmpty()) {
						out.write(mmQueue.poll());
					}
					try {
						Log.e("wifi", "sleep");
						synchronized (this) {
							wait();
						}
						Log.e("wifi", "wakeup");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} 
			catch (IOException e1) {
				sendMsgToHandler(IO_FAIL);
				Log.e("WIFI", "--- io exception ---", e1);
			} finally {
				close();
			}
		}
		
		public synchronized void send(byte[] buffer) {
			mmQueue.add(buffer);
			notifyAll();
		}
		
		public synchronized void close() {
			if (mmSocket != null) {
				try {
					mmSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			notifyAll();
		}
		
		public boolean isWorking() {
			return (mmSocket != null && mmSocket.isConnected() 
					&& !mmSocket.isClosed());
		}
		
		public void clearHandler() { mmHandler = null; }
		
		private void sendMsgToHandler(int what) {
			if(mmHandler != null) {
				mmHandler.obtainMessage(what).sendToTarget();
			}	
		}
	}
}
