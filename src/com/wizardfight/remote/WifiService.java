package com.wizardfight.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import android.util.Log;

public class WifiService {

	private static final int PORT = 8880;
	
	private static Worker mWorker;
	
	public static void init(String addr) {
		if(mWorker != null) {
			mWorker.close();
		}
		Log.d("wifi", "init "+addr);
		mWorker = new Worker(addr);
		//worker = new Worker("192.168.1.205");
		mWorker.start();
	}
	
	public static void send(byte[] buffer) {
		if(mWorker != null) {
			mWorker.send(buffer);
		}
	}
	
	static class Worker extends Thread {
		
		final String addr;
		Socket socket;
		final LinkedList<byte[]> queue;
		
		public Worker(String _addr) {
			addr = _addr;
			queue = new LinkedList<byte[]>();
		}
		
		public void run() {
			try {
				InetAddress serverAddr = InetAddress.getByName(addr);
				socket = new Socket(serverAddr, PORT);
				socket.setTcpNoDelay(true);
				OutputStream out = socket.getOutputStream();
				while(socket.isConnected()) {
					while(!queue.isEmpty()) {
						out.write(queue.poll());
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

			} catch (UnknownHostException e1) {
				Log.e("wifi", "unknown host", e1);
			} catch (IOException e1) {
				Log.e("wifi", "io exception", e1);
			}
		}
		
		public synchronized void send(byte[] buffer) {
			queue.add(buffer);
			notifyAll();
		}
		
		public synchronized void close() {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			notifyAll();
		}
	}
	
}
