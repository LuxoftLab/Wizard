package com.wizardfight;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/*
 * Represents a bot which fights via message handlers
 * (one handler to send messages, one to receive)
 */
class PlayerBot extends Thread  {
	private final static boolean D = false;
    private final static String TAG = "Wizard Fight Bot";
    private BotFightCore mCore;
    private Looper mLooper;

    public PlayerBot(Handler mainHandler, double botSpeedCoeff) {
        setName("player bot thread");
        mCore = new BotFightCore(mainHandler, botSpeedCoeff); 
    }

    public Handler getHandler() { return mCore.getHandler(); }
  
    public void run() {
        if(D) Log.d("Wizard Fight Bot", "Bot run");
        Looper.prepare();
        mLooper = Looper.myLooper();
        Looper.loop();
    }

    public void release() {
    	mCore.release();
        mLooper.quit();
        mLooper = null;
    }
}
