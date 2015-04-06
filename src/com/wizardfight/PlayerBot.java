package com.wizardfight;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.wizardfight.fight.Buff;
import com.wizardfight.fight.FightActivity;
import com.wizardfight.fight.FightCore;
import com.wizardfight.fight.FightMessage;
import com.wizardfight.fight.PlayerState;
import com.wizardfight.fight.FightCore.CoreAction;
import com.wizardfight.fight.FightCore.HandlerMessage;
import com.wizardfight.fight.FightMessage.*;
import com.wizardfight.remote.WifiService;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

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
