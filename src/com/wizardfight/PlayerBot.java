package com.wizardfight;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.wizardfight.FightMessage.*;
import com.wizardfight.FightActivity.AppMessage;
import com.wizardfight.remote.WifiService;

import java.util.Timer;
import java.util.TimerTask;

/*
 * Represents a bot which fights via message handlers
 * (one handler to send messages, one to receive)
 */
class PlayerBot extends Thread {
    private final static String TAG = "Wizard Fight Bot";
    private final int mStartHP;
    private final int mStartMana;
    private final double mTimeToThink = 0.5;
    private final double mK = 2;
    private Timer mTimer;
    private Looper mLooper;
    private PlayerState mSelfState;
    private PlayerState mEnemyState;
    private Handler mMainHandler;
    /**
     * The figure which is drawn at the moment by the bot.
     */
    private Shape shape;
    // The Handler that gets information back from the BluetoothChatService
    private Handler mHandler;

    public PlayerBot(int startHP, int startMana, Handler mainHandler) {
        setName("player bot thread");
        mMainHandler = mainHandler;
        mStartHP = startHP;
        mStartMana = startMana;
    }

    protected void initHandler() {
        Log.e("init","init");
        mHandler = new Handler() {
            /**
             * Sends a message.
             *
             * @param msg
             * A string of text to send.
             */

            @Override
            public void handleMessage(Message msg) {
                AppMessage appMsg = AppMessage.values()[msg.what];
                Log.e("player bot handler", appMsg.toString());

                switch (appMsg) {
                    case MESSAGE_FROM_SELF:
                        FightMessage selfMsg = (FightMessage) msg.obj;
                        handleSelfMessage(selfMsg);
                        break;
                    case MESSAGE_SELF_DEATH:
                        FightMessage selfDeath = new FightMessage(Target.ENEMY, FightAction.FIGHT_END);
                        sendFightMessage(selfDeath);
                        break;
                    case MESSAGE_FROM_ENEMY:
                        // message from main thread are coming as FightMessage objects
                        FightMessage enemyMsg = (FightMessage) msg.obj;
                        switch (enemyMsg.mAction) {
                            case FIGHT_START:
                                attack();
                                break;
                            case FIGHT_END:
                                finishFight(Target.SELF);
                                break;
                            default:
                                handleEnemyMessage(enemyMsg);
                        }
                        break;
                    case MESSAGE_MANA_REGEN:
                        mSelfState.manaTick();
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
                        Log.d("Wizard Fight", "Unknown message");
                        break;
                }
            }

            private void attack() {
                Log.e("1234", shape.toString());
                if (shape != Shape.NONE) {
                    FightMessage selfMsg = new FightMessage(shape);
                    boolean canBeCasted = mSelfState.requestSpell(selfMsg);
                    if (canBeCasted) {
                        if (selfMsg.mTarget == Target.SELF) {
                            handleMessageToSelf(selfMsg);
                        } else {
                            selfMsg.mTarget = Target.SELF;
                            sendFightMessage(selfMsg);
                        }
                    }
                }
                shape = Shape.NONE;
                if ((!mSelfState.hasBuff(Buff.CONCENTRATION))
                        && (canSpell(Shape.V, Shape.CIRCLE) && (mEnemyState.getHealth() > 30))) {
                    shape = Shape.V;
                } else {
                    if ((mEnemyState.hasBuff(Buff.HOLY_SHIELD)) && (canSpell(Shape.Z))) {
                        shape = Shape.Z;
                    } else {
                        if (canSpell(Shape.CIRCLE))
                            shape = Shape.CIRCLE;
                        else if ((canSpell(Shape.TRIANGLE)) && (mEnemyState.getHealth() < 10))
                            shape = Shape.TRIANGLE;
                    }
                }
                if ((mSelfState.getHealth() < mEnemyState.getHealth())
                        && (shape != Shape.TRIANGLE)
                        && (!mSelfState.hasBuff(Buff.HOLY_SHIELD))
                        && (!mSelfState.hasBuff(Buff.CONCENTRATION))
                        && (canSpell(Shape.SHIELD))) {
                    shape = Shape.SHIELD;
                }
                if ((shape != Shape.TRIANGLE) && (mSelfState.getHealth() < 40)) {
                    shape = Shape.CLOCK;
                }
                try {
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            attack();
                        }

                    }, (int) ((shape.getCastTime() + mTimeToThink) * 1000 * mK));
                } catch (IllegalStateException e) {
                    Log.w("PlayerBot","");
                }
            }

            private boolean canSpell(Shape... shapes) {
                int manaCost = 0;
                for (Shape shape : shapes) {
                    manaCost += shape.getManaCost();
                }
                return (manaCost < mSelfState.getMana());

            }

            private void handleSelfMessage(FightMessage selfMsg) {
                Log.d(TAG, "self msg : " + selfMsg);
                boolean canBeCasted = mSelfState.requestSpell(selfMsg);
                if (!canBeCasted)
                    return;

                if (selfMsg.mTarget == Target.SELF) {
                    // self influence to self
                    handleMessageToSelf(selfMsg);
                } else {
                    // self influence to enemy
                    // tell enemy : target is he
                    selfMsg.mTarget = Target.SELF;
                    sendFightMessage(selfMsg);
                }
            }

            private void handleEnemyMessage(FightMessage enemyMsg) {
                // refresh enemy health and mana (every enemy message contains it)
                mEnemyState.setHealthAndMana(enemyMsg.mHealth, enemyMsg.mMana);
                Log.d(TAG, "enemy msg: " + enemyMsg);
                if (enemyMsg.mTarget == Target.SELF) {
                    handleMessageToSelf(enemyMsg);
                } else {
                    // Enemy influence to himself
                    mEnemyState.handleSpell(enemyMsg);
                }
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
                }

                if (addedBuff != null) {
                    // buff added to player after spell (for example
                    // DoT, HoT, or shield),
                    // send message about enemy buff success
                    sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_ON,
                            addedBuff.ordinal());
                    sendFightMessage(sendMsg);
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
                }

                if (addedBuff == null && removedBuff == null) {
                    // nothing with buffs => just send self hp and mana to enemy
                    sendMsg = new FightMessage(Target.ENEMY,
                            FightAction.NEW_HP_OR_MANA, spellShape.ordinal());
                    sendFightMessage(sendMsg);
                }
            }

        };
    }

    void setupApp() {
        if(mTimer!=null)mTimer.cancel();
        mTimer = new Timer();
        shape = Shape.NONE;
        mEnemyState = new PlayerState(mStartHP, mStartMana, null);
        mSelfState = new PlayerState(mStartHP, mStartMana, mEnemyState);
        // Start mana regeneration
        mHandler.removeCallbacksAndMessages(null);
        mHandler.obtainMessage(AppMessage.MESSAGE_MANA_REGEN.ordinal(), null)
                .sendToTarget();
    }

    public void run() {
        Log.d("", "run");
        Log.e("player bot", Thread.currentThread().getName());
        Looper.prepare();
        mLooper = Looper.myLooper();
        initHandler();
        setupApp();
        Looper.loop();
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void release() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mSelfState = null;
        mEnemyState = null;
        mMainHandler = null;
        mLooper.quit();
        mLooper = null;
        mTimer.cancel();
        mTimer = null;
        shape = null;
    }

    private void finishFight(Target winner) {
        setupApp();
        if (winner != Target.SELF) {
            mHandler.obtainMessage(AppMessage.MESSAGE_SELF_DEATH.ordinal())
                    .sendToTarget();
        }
    }

    private void sendFightMessage(FightMessage msg) {
        msg.mHealth = mSelfState.getHealth();
        msg.mMana = mSelfState.getMana();
        msg.mIsBotMessage = true;

        // send to pc if connected
        WifiService.send(msg);
        
        byte[] sendBytes = msg.getBytes();
        mMainHandler.obtainMessage(AppMessage.MESSAGE_FROM_ENEMY.ordinal(), sendBytes)
                .sendToTarget();
    }
}
