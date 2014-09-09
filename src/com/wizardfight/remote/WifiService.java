package com.wizardfight.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import android.util.Log;

public class WifiService {

	public static final int PORT = 8880;
	
	private static Worker worker;
	
	public static void init(String addr) {
		if(worker != null) {
			worker.close();
		}
		Log.d("wifi", "init "+addr);
		worker = new Worker(addr);
		//worker = new Worker("192.168.1.205");
		worker.start();
	}
	
	public static void send(byte[] buffer) {
		if(worker != null) {
			worker.send(buffer);
		}
	}
	
	static class Worker extends Thread {
		
		String addr;
		Socket socket;
		LinkedList<byte[]> queue;
		
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
