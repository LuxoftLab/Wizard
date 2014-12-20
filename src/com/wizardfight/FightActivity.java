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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wizardfight.FightMessage.*;
import com.wizardfight.remote.WifiMessage;
import com.wizardfight.remote.WifiService;
import com.wizardfight.views.EnemyGUI;
import com.wizardfight.views.FightBackground;
import com.wizardfight.views.SelfGUI;

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
    // States of players
    private PlayerState mSelfState;
    private PlayerState mEnemyState;
    private boolean mAreMessagesBlocked;
    // Layout Views
    private Countdown mCountdown;
    private SelfGUI mSelfGUI;
    private EnemyGUI mEnemyGUI;

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
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "-- ON START --");
    }

    @Override
    public void onResume() {
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
    public void onStop() {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // remove all messages from handler
        WifiService.send(WifiMessage.LEAVE_FIGHT);
        mHandler.removeCallbacksAndMessages(null);
        Log.e(TAG, "--- ON DESTROY ---");
    }

    @Override
    protected Handler getHandler() {
        return new Handler() {
            /**
             * Sends a message.
             *
             * @param msg
             *            A string of text to send.
             */

            @Override
            public void handleMessage(Message msg) {
                AppMessage appMsg = AppMessage.values()[msg.what];

                switch (appMsg) {
                    case MESSAGE_STATE_CHANGE:
                        if (D)
                            Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:
                                // start fight
                                startFight();
                                break;
                            case BluetoothService.STATE_NONE:
                                break;
                        }
                        break;
                    case MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.connected_to) + mConnectedDeviceName,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case MESSAGE_TOAST:
                        Toast.makeText(getApplicationContext(),
                                msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case MESSAGE_COUNTDOWN_END:
                        mAreMessagesBlocked = false;
                        break;
                    case MESSAGE_CONNECTION_FAIL:
                        Toast.makeText(getApplicationContext(),
                                msg.getData().getInt(TOAST), Toast.LENGTH_SHORT).show();
                        mAreMessagesBlocked = true;
                        finish();
                        break;
                    case MESSAGE_FROM_SELF:
                        if (mAreMessagesBlocked)
                            return;
                        FightMessage selfMsg = (FightMessage) msg.obj;
                        handleSelfMessage(selfMsg);
                        break;
                    case MESSAGE_SELF_DEATH:
                        Log.e("azaza", "send fight end message to enemy");
                        FightMessage selfDeath = new FightMessage(Target.ENEMY,
                                FightAction.FIGHT_END);
                        sendFightMessage(selfDeath);
                        break;
                    case MESSAGE_FROM_ENEMY:
                        byte[] recvBytes = (byte[]) msg.obj;
                        FightMessage enemyMsg = FightMessage.fromBytes(recvBytes);

                        switch (enemyMsg.mAction) {
                            case ENEMY_READY:
                                handleEnemyReadyMessage();
                                break;
                            case FIGHT_START:
                                startFight();
                                break;
                            case FIGHT_END:
                                Log.e("azaza", "MESSAGE FIGHT END!!!");
                                finishFight(Target.SELF);
                                break;
                            default:
                                if (mAreMessagesBlocked)
                                    return;
                                handleEnemyMessage(enemyMsg);
                        }
                        break;
                    case MESSAGE_MANA_REGEN:
                        mSelfState.manaTick();
                        mSelfGUI.getManaBar().setValue(mSelfState.getMana());
                        // inform enemy about new mana
                        FightMessage fMsg = new FightMessage(Target.ENEMY,
                                FightAction.NEW_HP_OR_MANA, Shape.NONE.ordinal());
                        sendFightMessage(fMsg);
                        // send next tick after 2 sec
                        Message msgManaReg = this.obtainMessage(
                                AppMessage.MESSAGE_MANA_REGEN.ordinal(), 0, 0, null);
                        this.sendMessageDelayed(msgManaReg, 2000);
                        break;
                    default:
                        if (D)
                            Log.e("Wizard Fight", "Unknown message");
                        break;
                }
            }

            private void handleSelfMessage(FightMessage selfMsg) {
                Shape sendShape = FightMessage.getShapeFromMessage(selfMsg);
                if (sendShape != Shape.NONE) {
                    mIsCastAbilityBlocked = false;
                }
                // mSelfGUI.log("self msg : " + selfMsg + " " + (mMyCounter++));
                Log.e(TAG, "self msg : " + selfMsg + " " + mMyCounter);
                // request mana for spell
                boolean canBeCasted = mSelfState.requestSpell(selfMsg);

                if (!canBeCasted) {
                    if (mSensorAndSoundThread != null) {
                        mSensorAndSoundThread.playNoManaSound();
                    }
                    return;
                }

                if (mSensorAndSoundThread != null) {
                    mSensorAndSoundThread.playShapeSound(sendShape);
                }

                mSelfGUI.getManaBar().setValue(mSelfState.getMana());

                if (selfMsg.mTarget == Target.SELF) {
                    // self influence to self
                    handleMessageToSelf(selfMsg);
                } else {
                    // self influence to enemy
                    // tell enemy : target is he
                    selfMsg.mTarget = Target.SELF;
                    sendFightMessage(selfMsg);
                }
                // draw casted shape
                if (sendShape != Shape.NONE) {
                    mSelfGUI.getSpellPicture().setShape(sendShape);
                }
            }

            private void handleEnemyMessage(FightMessage enemyMsg) {

                Shape recvShape = FightMessage.getShapeFromMessage(enemyMsg);

                // refresh enemy health and mana (every enemy message contains it)
                mEnemyState.setHealthAndMana(enemyMsg.mHealth, enemyMsg.mMana);
                Log.e(TAG, "enemy msg: " + enemyMsg + " " + mMyCounter);
                if (enemyMsg.mTarget == Target.SELF) {
                    handleMessageToSelf(enemyMsg);
                } else {
                    // Enemy influence to himself
                    mEnemyState.handleSpell(enemyMsg);

                    if (mEnemyState.getRemovedBuff() != null) {
                        // remove buff from enemy GUI
                        mEnemyGUI.getBuffPanel().removeBuff(
                                mEnemyState.getRemovedBuff());
                    }

                    if (mEnemyState.getAddedBuff() != null) {
                        // add buff to enemy GUI
                        mEnemyGUI.getBuffPanel()
                                .addBuff(mEnemyState.getAddedBuff());
                    }
                }

                // refresh enemy
                if (FightMessage.isSpellCreatedByEnemy(enemyMsg)) {
                    mEnemyGUI.getSpellPicture().setShape(recvShape);
                }
                mEnemyGUI.getHealthBar().setValue(mEnemyState.getHealth());
                mEnemyGUI.getManaBar().setValue(mEnemyState.getMana());
            }

            private void handleMessageToSelf(FightMessage fMessage) {
                FightMessage sendMsg;
                // Enemy influence to player
                mSelfState.handleSpell(fMessage);
                if (mSelfState.getHealth() == 0) {
                    finishFight(Target.ENEMY);
                    return;
                }

                Buff addedBuff = mSelfState.getAddedBuff();
                Buff removedBuff = mSelfState.getRemovedBuff();
                Buff refreshedBuff = mSelfState.getRefreshedBuff();
                Shape spellShape = mSelfState.getSpellShape();

                if (removedBuff != null) {
                    // buff was removed after spell,
                    // send message about buff loss to enemy
                    sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_OFF,
                            removedBuff.ordinal());
                    sendFightMessage(sendMsg);
                    // remove buff from panel
                    mSelfGUI.getBuffPanel().removeBuff(removedBuff);
                    if (mSelfState.isBuffRemovedByEnemy() && mSensorAndSoundThread != null) {
                        mSensorAndSoundThread.playBuffSound(removedBuff);
                    }
                }

                if (addedBuff != null) {
                    // buff added to player after spell (for example
                    // DoT, HoT, or shield),
                    // send message about enemy buff success
                    sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_ON,
                            addedBuff.ordinal());
                    sendFightMessage(sendMsg);
                    // add buff to panel
                    mSelfGUI.getBuffPanel().addBuff(addedBuff);
                    Log.e("azaza", "! send buff on" + sendMsg);
                }

                if (addedBuff != null || refreshedBuff != null) {
                    // send message of the buff tick
                    if (addedBuff != null)
                        refreshedBuff = addedBuff;
                    FightMessage fm = new FightMessage(Target.SELF,
                            FightAction.BUFF_TICK, refreshedBuff.ordinal());
                    Message buffTickMsg = this.obtainMessage(
                            AppMessage.MESSAGE_FROM_SELF.ordinal(), fm);
                    this.sendMessageDelayed(buffTickMsg,
                            refreshedBuff.getDuration());
                    Log.e("azaza", "! send buff tick " + buffTickMsg);
                }

                if (addedBuff == null && removedBuff == null) {
                    // nothing with buffs => just send self hp and mana to enemy
                    sendMsg = new FightMessage(Target.ENEMY,
                            FightAction.NEW_HP_OR_MANA, spellShape.ordinal());
                    sendFightMessage(sendMsg);
                }

                mSelfGUI.getHealthBar().setValue(mSelfState.getHealth());
                mSelfGUI.getManaBar().setValue(mSelfState.getMana());
            }

        };
    }


    void setupApp() {
        if (D)
            Log.d(TAG, "setupApp()");
        // for debugging
        mMyCounter = 0;
        // Create players states
        mEnemyState = new PlayerState(PLAYER_HP, PLAYER_MANA, null);
        mSelfState = new PlayerState(PLAYER_HP, PLAYER_MANA, mEnemyState);
        // Initialize players UI
        mSelfGUI = new SelfGUI(this, PLAYER_HP, PLAYER_MANA);
        mEnemyGUI = new EnemyGUI(this, PLAYER_HP, PLAYER_MANA);
        // Drop flags
        mAreMessagesBlocked = true;
        // Last touch value
        mLastTouchAction = MotionEvent.ACTION_UP;
        // Start mana regeneration
        mHandler.removeMessages(AppMessage.MESSAGE_MANA_REGEN.ordinal());
        mHandler.obtainMessage(AppMessage.MESSAGE_MANA_REGEN.ordinal(), null)
                .sendToTarget();
    }

    void sendFightMessage(FightMessage fMessage) {
        Log.e("azaza", "send fight message: " + fMessage);
        // always send own health and mana
        fMessage.mHealth = mSelfState.getHealth();
        fMessage.mMana = mSelfState.getMana();
        fMessage.mIsBotMessage = false;
    }

    protected void startFight() {
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

    private void finishFight(Target winner) {
        Log.e("azaza", "FINISH FIGHT");
        mAreMessagesBlocked = true;

        stopSensorAndSound();

        mSensorAndSoundThread = new SensorAndSoundThread(this, mSensorManager,
                mAccelerometer);
        mSensorAndSoundThread.start();
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
            Log.e("azaza", "send MESSAGE_SELF_DEATH to myself");
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
            Log.e(TAG, "INIT FIGHT");
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

    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
//        Log.e(TAG, "-- WINDOW FOCUS CHANGED --");

        ImageView indicator = (ImageView) findViewById(R.id.imageView);
        int w = indicator.getWidth();
        int h = indicator.getHeight();
        int barW = w*2/3, barH = h*2/10;
        int barMarginW = w*8/1000, hpMarginH = h*15/100, mpMarginH = h*36/100;
        int picSize = w*37/100, picMarginH = w*14/1000;
        int buffSize = h*2/10, buffMargin = 5;
        View hpself =  findViewById(R.id.self_health);
        View mpself = findViewById(R.id.self_mana);
        View hpenemy = findViewById(R.id.enemy_health);
        View mpenemy = findViewById(R.id.enemy_mana);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(barW, barH);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.imageView);
        params.addRule(RelativeLayout.ALIGN_LEFT, R.id.imageView);
        params.setMargins(barMarginW, hpMarginH, 0, 0);
        hpself.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(barW, barH);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.imageView);
        params.addRule(RelativeLayout.ALIGN_LEFT, R.id.imageView);
        params.setMargins(barMarginW, mpMarginH, 0, 0);
        mpself.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(barW, barH);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.imageView2);
        params.addRule(RelativeLayout.ALIGN_LEFT, R.id.imageView2);
        params.setMargins(barMarginW, hpMarginH, 0, 0);
        hpenemy.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(barW, barH);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.imageView2);
        params.addRule(RelativeLayout.ALIGN_LEFT, R.id.imageView2);
        params.setMargins(barMarginW, mpMarginH, 0, 0);
        mpenemy.setLayoutParams(params);

        View spell = findViewById(R.id.self_spell);
        params = new RelativeLayout.LayoutParams(picSize, picSize);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.imageView);
        params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.imageView);
        params.setMargins(0, picMarginH, picMarginH, 0);
        spell.setLayoutParams(params);

        spell = findViewById(R.id.enemy_spell);
        params = new RelativeLayout.LayoutParams(picSize, picSize);
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.imageView2);
        params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.imageView2);
        params.setMargins(0, picMarginH, picMarginH, 0);
        spell.setLayoutParams(params);

        int[] buffs = {R.id.self_buff1, R.id.self_buff2, R.id.self_buff3, R.id.self_buff4, R.id.self_buff5,
                R.id.enemy_buff1, R.id.enemy_buff2, R.id.enemy_buff3, R.id.enemy_buff4, R.id.enemy_buff5};
        View buff;
        LinearLayout.LayoutParams params1;
        for (int buff1 : buffs) {
            buff = findViewById(buff1);
            params1 = new LinearLayout.LayoutParams(buffSize, buffSize);
            params1.setMargins(buffMargin, buffMargin, buffMargin, buffMargin);
            buff.setLayoutParams(params1);
        }
    }

}