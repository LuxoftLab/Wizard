package com.wizardfight.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import android.os.Handler;
import android.util.Log;

public class WifiService {
	public static final int INIT_NO_ERROR = 0;
	public static final int INIT_FAILED = 1;
	public static final int PORT = 8880;
	private static Worker mWorker;
	
	public static void init(String addr, Handler handler) {
		close();
		Log.d("wifi", "init "+addr);
		mWorker = new Worker(addr, handler);
		mWorker.start();
	}
	
	public static void send(byte[] buffer) {
		if(mWorker != null) {
			mWorker.send(buffer);
		}
	}
	
	public static boolean isConnected() {
		if(mWorker == null) return false;
		return mWorker.isAlive();
	}
	
	public static void close() {
		if(mWorker != null) {
			mWorker.close();
		}
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
				sendMsgToHandler(INIT_NO_ERROR);
				
				OutputStream out = mmSocket.getOutputStream();
				while(mmSocket.isConnected()) {
					while(!mmQueue.isEmpty()) {
						out.write(mmQueue.poll());
					}
					try {
						Log.d("wifi", "sleep");
						synchronized (this) {
							wait();
						}
						Log.d("wifi", "wakeup");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e1) {
				sendMsgToHandler(INIT_FAILED);
				Log.e("WIFI", "--- io exception ---", e1);
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
		
		private void sendMsgToHandler(int what) {
			mmHandler.obtainMessage(what).sendToTarget();
		}
	}
}
