package com.wizardfight;

import com.wizardfight.FightActivity.AppMessage;
import com.wizardfight.FightMessage.FightAction;
import com.wizardfight.FightMessage.Target;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class FightCore extends Handler {
	// States of players
    private PlayerState mSelfState;
    private PlayerState mEnemyState;
    
    public FightCore() {
    	// Create players states
        mEnemyState = new PlayerState(FightActivity.PLAYER_HP, FightActivity.PLAYER_MANA, null);
        mSelfState = new PlayerState(FightActivity.PLAYER_HP, FightActivity.PLAYER_MANA, mEnemyState);
    }
    @Override
    public void handleMessage(Message msg) {
        AppMessage appMsg = AppMessage.values()[msg.what];

        switch (appMsg) {
            case MESSAGE_STATE_CHANGE:
            	//TODO notify 
//            	onBluetoothStateChange(msg.arg1);
                break;
            case MESSAGE_DEVICE_NAME:
            	//TODO notify 
//                String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(),
//                        getString(R.string.connected_to) + mConnectedDeviceName,
//                        Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
            	//TODO notify 
//                Toast.makeText(getApplicationContext(),
//                        msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
//                        .show();
                break;
            case MESSAGE_COUNTDOWN_END:
            	//TODO notify 
//                mAreMessagesBlocked = false;
                break;
            case MESSAGE_CONNECTION_FAIL:
            	//TODO notify 
//                Toast.makeText(getApplicationContext(),
//                        msg.getData().getInt(TOAST), Toast.LENGTH_SHORT).show();
//                mAreMessagesBlocked = true;
//                finish();
                break;
            case MESSAGE_FROM_SELF:
            	//TODO notify
//                if (mAreMessagesBlocked) return;
//                FightMessage selfMsg = (FightMessage) msg.obj;
//                handleSelfMessage(selfMsg);
                break;
            case MESSAGE_SELF_DEATH:
            	//TODO notify
//                FightMessage selfDeath = new FightMessage(Target.ENEMY,
//                        FightAction.FIGHT_END);
//                sendFightMessage(selfDeath);
                break;
            case MESSAGE_FROM_ENEMY:
            	//TODO notify
//                byte[] recvBytes = (byte[]) msg.obj;
//                FightMessage enemyMsg = FightMessage.fromBytes(recvBytes);
//
//                switch (enemyMsg.mAction) {
//                    case ENEMY_READY:
//                        handleEnemyReadyMessage();
//                        break;
//                    case FIGHT_START:
//                        startFight();
//                        break;
//                    case FIGHT_END:
//                        finishFight(Target.SELF);
//                        break;
//                    default:
//                        if (mAreMessagesBlocked) return;
//                        handleEnemyMessage(enemyMsg);
//                }
//                break;
//            case MESSAGE_MANA_REGEN:
//                handleManaRegen();
//                break;
//            default:
//                break;
        }
    }

    private void handleManaRegen() {
    	//TODO notify
//    	mSelfState.manaTick();
//        mSelfGUI.getManaBar().setValue(mSelfState.getMana());
//        // inform enemy about new mana
//        FightMessage fMsg = new FightMessage(Target.ENEMY,
//                FightAction.NEW_HP_OR_MANA, Shape.NONE.ordinal());
//        sendFightMessage(fMsg);
//        // send next tick after 2 sec
//        Message msgManaReg = this.obtainMessage(
//                AppMessage.MESSAGE_MANA_REGEN.ordinal(), 0, 0, null);
//        this.sendMessageDelayed(msgManaReg, 2000);
    }
    
    private void handleSelfMessage(FightMessage selfMsg) {
    	//TODO notify
//        Shape sendShape = FightMessage.getShapeFromMessage(selfMsg);
//        if (sendShape != Shape.NONE) {
//            mIsCastAbilityBlocked = false;
//        }
//        if (D) Log.e(TAG, "self msg : " + selfMsg + " " + mMyCounter);
//        // request mana for spell
//        boolean canBeCasted = mSelfState.requestSpell(selfMsg);
//
//        if (!canBeCasted) {
//        	FightSound.playNoManaSound();
//            return;
//        }
//
//        FightSound.playShapeSound(sendShape);
//
//        mSelfGUI.getManaBar().setValue(mSelfState.getMana());
//
//        if (selfMsg.mTarget == Target.SELF) {
//            // self influence to self
//            handleMessageToSelf(selfMsg);
//        } else {
//            // self influence to enemy
//            // tell enemy : target is he
//            selfMsg.mTarget = Target.SELF;
//            sendFightMessage(selfMsg);
//        }
//        // draw casted shape
//        if (sendShape != Shape.NONE) {
//            mSelfGUI.getSpellPicture().setShape(sendShape);
//        }
    }

    private void handleEnemyMessage(FightMessage enemyMsg) {
    	//TODO notify
//        Shape recvShape = FightMessage.getShapeFromMessage(enemyMsg);
//
//        // refresh enemy health and mana (every enemy message contains it)
//        mEnemyState.setHealthAndMana(enemyMsg.mHealth, enemyMsg.mMana);
//        if (D) Log.e(TAG, "enemy msg: " + enemyMsg + " " + mMyCounter);
//        if (enemyMsg.mTarget == Target.SELF) {
//            handleMessageToSelf(enemyMsg);
//        } else {
//            // Enemy influence to himself
//            mEnemyState.handleSpell(enemyMsg);
//
//            if (mEnemyState.getRemovedBuff() != null) {
//                // remove buff from enemy GUI
//                mEnemyGUI.getBuffPanel().removeBuff(
//                        mEnemyState.getRemovedBuff());
//            }
//
//            if (mEnemyState.getAddedBuff() != null) {
//                // add buff to enemy GUI
//                mEnemyGUI.getBuffPanel()
//                        .addBuff(mEnemyState.getAddedBuff());
//            }
//        }
//
//        // refresh enemy
//        if (FightMessage.isSpellCreatedByEnemy(enemyMsg)) {
//            mEnemyGUI.getSpellPicture().setShape(recvShape);
//        }
//        mEnemyGUI.getHealthBar().setValue(mEnemyState.getHealth());
//        mEnemyGUI.getManaBar().setValue(mEnemyState.getMana());
    }

    private void handleMessageToSelf(FightMessage fMessage) {
    	//TODO notify
//        FightMessage sendMsg;
//        // Enemy influence to player
//        mSelfState.handleSpell(fMessage);
//        if (mSelfState.getHealth() == 0) {
//            finishFight(Target.ENEMY);
//            return;
//        }
//
//        Buff addedBuff = mSelfState.getAddedBuff();
//        Buff removedBuff = mSelfState.getRemovedBuff();
//        Buff refreshedBuff = mSelfState.getRefreshedBuff();
//        Shape spellShape = mSelfState.getSpellShape();
//
//        if (removedBuff != null) {
//            // buff was removed after spell,
//            // send message about buff loss to enemy
//            sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_OFF,
//                    removedBuff.ordinal());
//            sendFightMessage(sendMsg);
//            // remove buff from panel
//            mSelfGUI.getBuffPanel().removeBuff(removedBuff);
//            if (mSelfState.isBuffRemovedByEnemy()) {
//                FightSound.playBuffSound(removedBuff);
//            }
//        }
//
//        if (addedBuff != null) {
//            // buff added to player after spell (for example
//            // DoT, HoT, or shield),
//            // send message about enemy buff success
//            sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_ON,
//                    addedBuff.ordinal());
//            sendFightMessage(sendMsg);
//            // add buff to panel
//            mSelfGUI.getBuffPanel().addBuff(addedBuff);
//        }
//
//        if (addedBuff != null || refreshedBuff != null) {
//            // send message of the buff tick
//            if (addedBuff != null)
//                refreshedBuff = addedBuff;
//            FightMessage fm = new FightMessage(Target.SELF,
//                    FightAction.BUFF_TICK, refreshedBuff.ordinal());
//            Message buffTickMsg = this.obtainMessage(
//                    AppMessage.MESSAGE_FROM_SELF.ordinal(), fm);
//            this.sendMessageDelayed(buffTickMsg,
//                    refreshedBuff.getDuration());
//        }
//
//        if (addedBuff == null && removedBuff == null) {
//            // nothing with buffs => just send self hp and mana to enemy
//            sendMsg = new FightMessage(Target.ENEMY,
//                    FightAction.NEW_HP_OR_MANA, spellShape.ordinal());
//            sendFightMessage(sendMsg);
//        }
//
//        mSelfGUI.getHealthBar().setValue(mSelfState.getHealth());
//        mSelfGUI.getManaBar().setValue(mSelfState.getMana());
    }
    	
    public int getHealth() { return mSelfState.getHealth(); }
    public int getMana() { return mSelfState.getMana(); }
}
