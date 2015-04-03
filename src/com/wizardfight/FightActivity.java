package com.wizardfight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wizardfight.FightMessage.*;
import com.wizardfight.remote.WifiMessage;
import com.wizardfight.remote.WifiService;
import com.wizardfight.views.BuffPanel;
import com.wizardfight.views.BuffPicture;
import com.wizardfight.views.FightBackground;
import com.wizardfight.views.PlayerGUI;

/**
 * Extends CastActivity for two players fighting.
 * Adds two player states with current hp, mana, activated buffs...
 * Handles players messages (implements fight logic)
 * Adds countdown at start and dialog for restart.
 */
public abstract class FightActivity extends CastActivity {

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final int PLAYER_HP = 200;
    public static final int PLAYER_MANA = 100;

    //private Dialog mNetDialog;
    FightEndDialog mFightEndDialog;
    // Debugging
    private int mMyCounter;
    // is activity running
    private boolean mIsRunning;
    private boolean mAreMessagesBlocked;
    // Layout Views
    private Countdown mCountdown;
    private PlayerGUI mSelfGUI;
    private PlayerGUI mEnemyGUI;

    // test mode dialog with spell names
    private FightBackground mBgImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D)
            Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.fight);

        // add countdown view to the top
        LayoutInflater inflater = getLayoutInflater();
        View countdownView = inflater.inflate(R.layout.countdown, null);
        mCountdown = new Countdown(this, countdownView, mHandler);
        getWindow().addContentView(
                countdownView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT));

        // Init on touch listener
        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.fight_layout_root);
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mAreMessagesBlocked || mIsCastAbilityBlocked)
                    return true;
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP
                        || action == MotionEvent.ACTION_DOWN) {
                    if (mLastTouchAction == action) {
                        return true;
                    }
                    if (action == MotionEvent.ACTION_DOWN) {
                        mBgImage.toBright();
                    } else {
                        mBgImage.toDark();
                    }
                    buttonClick();
                    mLastTouchAction = action;
                }
                return false;
            }
        });

        // Initialize GUI and logic
        setupApp();
        // Initialize end dialog object
        mBgImage = (FightBackground) findViewById(R.id.fight_background);
    }

    @Override
    public void onResume() {
    	if (D) Log.e(TAG, "--- ON RESUME ---");
        mIsRunning = true;
        super.onResume();
        mBgImage.darkenImage();

        if (mFightEndDialog.isNeedToShow()) {
            mFightEndDialog.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsRunning = false;
    }

    @Override
    public void onDestroy() {
    	if (D) Log.e(TAG, "--- ON DESTROY FIGHT ACTIVITY ---");
        super.onDestroy();
        // remove all messages from handler
        WifiService.send(WifiMessage.LEAVE_FIGHT);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected FightCore getHandler() {
       return new FightCore();
    }


    void setupApp() {
        if (D)
            Log.d(TAG, "setupApp()");
        // for debugging
        mMyCounter = 0;
        
        // Initialize players UI
        findViewById(R.id.self_health);
        mSelfGUI = new PlayerGUI(
                findViewById(R.id.self_health), findViewById(R.id.self_mana), findViewById(R.id.self_spell), findViewById(R.id.imageView),
                new BuffPanel((BuffPicture)findViewById(R.id.self_buff1),
                        (BuffPicture)findViewById(R.id.self_buff2),
                        (BuffPicture)findViewById(R.id.self_buff3),
                        (BuffPicture)findViewById(R.id.self_buff4),
                        (BuffPicture)findViewById(R.id.self_buff5)));
        mEnemyGUI = new PlayerGUI(
                findViewById(R.id.enemy_health), findViewById(R.id.enemy_mana), findViewById(R.id.enemy_spell), findViewById(R.id.imageView2),
                new BuffPanel((BuffPicture)findViewById(R.id.enemy_buff1),
                        (BuffPicture)findViewById(R.id.enemy_buff2),
                        (BuffPicture)findViewById(R.id.enemy_buff3),
                        (BuffPicture)findViewById(R.id.enemy_buff4),
                        (BuffPicture)findViewById(R.id.enemy_buff5)));
        // Drop flags
        mAreMessagesBlocked = true;
        // Last touch value
        mLastTouchAction = MotionEvent.ACTION_UP;
    }

    void sendFightMessage(FightMessage fMessage) {
    	if (D) Log.e("azaza", "send fight message: " + fMessage);
        // always send own health and mana
        fMessage.mHealth = mSelfState.getHealth();
        fMessage.mMana = mSelfState.getMana();
        fMessage.mIsBotMessage = false;
    }

    protected void startFight() {
        // Start mana regeneration
        if (mHandler != null) {
            mHandler.removeMessages(AppMessage.MESSAGE_MANA_REGEN.ordinal());
            mHandler.obtainMessage(AppMessage.MESSAGE_MANA_REGEN.ordinal(), null)
                    .sendToTarget();
        }
        // start countdown
        if (D)
            Log.e(TAG, "before start countdown");

        mCountdown.startCountdown();
        if (D)
            Log.e(TAG, "after start countdown");
        if (D)
            Log.e(TAG, "accelerator thread all stuff called");
    }

    abstract void handleEnemyReadyMessage();

    abstract void onBluetoothStateChange(int state);
    
    private void finishFight(Target winner) {
    	if (D) Log.e(TAG, "FINISH FIGHT");
        mAreMessagesBlocked = true;

        stopSensorAndSound();
        startNewSensorAndSound();
        // set GUI to initial state
        mBgImage.darkenImage();
        mSelfGUI.clear();
        mEnemyGUI.clear();
        // Recreate objects
        setupApp();

        String message;
        if (winner == Target.SELF) {
            message = getString(R.string.you_win);
        } else {
            // we must inform enemy about loss
            mHandler.obtainMessage(AppMessage.MESSAGE_SELF_DEATH.ordinal())
                    .sendToTarget();
            message = getString(R.string.you_lose);
        }

        mFightEndDialog.init(message);
        // consider the dialog call while activity is not running
        if (mIsRunning) {
            mFightEndDialog.show();
        } else {
            mFightEndDialog.setNeedToShow(true);
        }
    }


    // Message types sent from the BluetoothChatService Handler
    enum AppMessage {
        MESSAGE_STATE_CHANGE, MESSAGE_READ, MESSAGE_WRITE, MESSAGE_DEVICE_NAME, MESSAGE_TOAST, MESSAGE_COUNTDOWN_END, MESSAGE_CONNECTION_FAIL, MESSAGE_FROM_SELF, MESSAGE_SELF_DEATH, MESSAGE_FROM_ENEMY, MESSAGE_MANA_REGEN
    }

    abstract class FightEndDialog implements DialogInterface.OnClickListener {
        AlertDialog mmDialog;
        boolean mmIsNeedToShow;

        public void init(String message) {
        	if (D) Log.e(TAG, "INIT FIGHT END DIALOG");
            mmIsNeedToShow = false;
            mmDialog.setTitle(getString(R.string.fight_ended));
            mmDialog.setMessage(message);
            mmDialog.setButton(getString(R.string.restart), this);
            mmDialog.setButton2(getString(R.string.exit), this);
            mmDialog.setCancelable(false);
        }

        public boolean isNeedToShow() {
            return mmIsNeedToShow;
        }

        public void setNeedToShow(boolean isNeed) {
            mmIsNeedToShow = isNeed;
        }

        public void show() {
            mmDialog.show();
        }

        @Override
        abstract public void onClick(DialogInterface dialog, int which);
    }

}