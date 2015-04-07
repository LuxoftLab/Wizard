package com.wizardfight.fight;

import java.util.Observable;

import com.wizardfight.Sound;
import com.wizardfight.Shape;
import com.wizardfight.fight.FightMessage.Target;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FightCore extends Observable {
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	protected boolean mAreMessagesBlocked;
	// States of players
	protected PlayerState mSelfState;
	protected PlayerState mEnemyState;
	private ObservableData mData;

	public static class ObservableData {
		// data storage for observers
		private int btState;
		private String deviceName;
		private String infoString;
		private int failStringId;
		private FightMessage messageToSend;
		private Target winner;
		private Shape selfShape;
		private Shape enemyShape;
		
		public int getBluetoothState() { return btState; }

		public String getDeviceName() { return deviceName; }

		public String getInfoString() { return infoString; }
		
		public int getFailStringId() { return failStringId; }
		
		public FightMessage getMessageToSend() { return messageToSend; }
		
		public Target getWinner() {  return winner; }
		
		public Shape getSelfShape() { return selfShape; }
		
		public Shape getEnemyShape() { return enemyShape; }
	}

	// Message types sent from the BluetoothChatService Handler
	public enum HandlerMessage {
		HM_BT_STATE_CHANGE, HM_DEVICE_NAME, HM_TOAST, HM_COUNTDOWN_END, HM_CONNECTION_FAIL, HM_FROM_SELF, HM_SELF_DEATH, HM_FROM_ENEMY, HM_MANA_REGEN
	}

	public enum CoreAction {
		CM_BT_STATE_CHANGE, CM_DEVICE_NAME, CM_INFO_STRING, CM_COUNTDOWN_END, CM_CONNECTION_FAIL, 
		CM_MESSAGE_TO_SEND, CM_ENEMY_READY, CM_FIGHT_START, CM_HEALTH_CHANGED, CM_MANA_CHANGED, 
		CM_SELF_CAST, CM_SELF_CAST_SUCCESS, CM_SELF_BUFF_TICK, CM_SELF_CAST_NOMANA, CM_NEW_BUFF, CM_REMOVED_BUFF, CM_ENEMY_CAST,
		CM_ENEMY_HEALTH_MANA, CM_ENEMY_NEW_BUFF, CM_ENEMY_REMOVED_BUFF, CM_ENEMY_BUFF_TICK, CM_FIGHT_END;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			HandlerMessage appMsg = HandlerMessage.values()[msg.what];
			if (FightActivity.D) {
				Log.e("Wizard Fight", "msg blocked? : " + mAreMessagesBlocked + " [Fight Core]");
				Log.e("Wizard Fight", "hm: " + appMsg.name() + " [Fight Core]");
			}
			
			switch (appMsg) {
			case HM_BT_STATE_CHANGE:
				onBluetoothStateChange(msg.arg1);
				break;
			case HM_DEVICE_NAME:
				onDeviceName(msg.getData().getString(DEVICE_NAME));
				break;
			case HM_TOAST:
				onInfo(msg.getData().getString(TOAST));
				break;
			case HM_COUNTDOWN_END:
				onCountdownEnd();
				break;
			case HM_CONNECTION_FAIL:
				onConnectionFail(msg.getData().getInt(TOAST));
				break;
			case HM_FROM_SELF:
				onSelfMessage((FightMessage) msg.obj);
				break;
			case HM_SELF_DEATH:
				onSelfDeath();
				break;
			case HM_FROM_ENEMY:
				byte[] recvBytes = (byte[]) msg.obj;
				FightMessage enemyMsg = FightMessage.fromBytes(recvBytes);
				onEnemyMessage(enemyMsg);
				break;
			case HM_MANA_REGEN:
				onManaRegen();
				break;
			}
		}
	};

	public FightCore() {
		mData = new ObservableData();
		init();
	}

	public void init() {
		// Create players states
		mEnemyState = new PlayerState(FightActivity.PLAYER_HP,
				FightActivity.PLAYER_MANA, null);
		mSelfState = new PlayerState(FightActivity.PLAYER_HP,
				FightActivity.PLAYER_MANA, mEnemyState);
		mAreMessagesBlocked = true;
	}

	protected void onBluetoothStateChange(int state) {
		mData.btState = state;
		share(CoreAction.CM_BT_STATE_CHANGE);
	}

	protected void onDeviceName(String name) {
		mData.deviceName = name;
		share(CoreAction.CM_DEVICE_NAME);
	}

	protected void onInfo(String str) {
		mData.infoString = str;
		share(CoreAction.CM_INFO_STRING);
	}

	protected void onCountdownEnd() {
		mAreMessagesBlocked = false;
		share(CoreAction.CM_COUNTDOWN_END);
	}

	protected void onConnectionFail(int stringResId) {
		mData.failStringId = stringResId;
		mAreMessagesBlocked = true;
		share(CoreAction.CM_CONNECTION_FAIL);
	}

	protected void onSelfDeath() {
		Log.e("Wizard Fight", "---------------------> SELF DEATH");
		FightMessage selfDeath = new FightMessage(Target.ENEMY,
				CoreAction.CM_FIGHT_END);
		sendFightMessage(selfDeath);
	}

	protected void sendFightMessage(FightMessage msg) {
		msg.mHealth = mSelfState.getHealth();
		msg.mMana = mSelfState.getMana();
		msg.mIsBotMessage = false;
		mData.messageToSend = msg;
		share(CoreAction.CM_MESSAGE_TO_SEND);
	}

	protected void onEnemyMessage(FightMessage enemyMsg) {
		switch (enemyMsg.mAction) {
		case CM_ENEMY_READY:
			onEnemyReadyMessage();
			break;
		case CM_FIGHT_START:
			startFight();
			break;
		case CM_FIGHT_END:
			finishFight(Target.SELF);
			break;
		default:
			if (mAreMessagesBlocked)
				return;
			onEnemyFightMessage(enemyMsg);
		}
	}

	protected void onEnemyReadyMessage() {
		share(CoreAction.CM_ENEMY_READY);
	}

	protected void startFight() {
		// Start mana regeneration
		if (mHandler != null) {
			mHandler.removeMessages(HandlerMessage.HM_MANA_REGEN.ordinal());
			mHandler.obtainMessage(HandlerMessage.HM_MANA_REGEN.ordinal(), null)
					.sendToTarget();
		}
		share(CoreAction.CM_FIGHT_START);
	}

	private void onManaRegen() {
		// TODO notify
		mSelfState.manaTick();

		// inform enemy about new mana
		FightMessage fMsg = new FightMessage(Target.ENEMY,
				CoreAction.CM_ENEMY_HEALTH_MANA, Shape.NONE.ordinal());
		sendFightMessage(fMsg);

		// send next tick after 2 sec
		Message msgManaReg = mHandler.obtainMessage(
				HandlerMessage.HM_MANA_REGEN.ordinal(), 0, 0, null);
		mHandler.sendMessageDelayed(msgManaReg, 2000);

		share(CoreAction.CM_MANA_CHANGED);
	}

	protected void onSelfMessage(FightMessage selfMsg) {
		if (mAreMessagesBlocked)
			return;

		mData.selfShape = FightMessage.getShapeFromMessage(selfMsg);
		boolean canBeCasted = mSelfState.requestSpell(selfMsg);

		if (!canBeCasted) {
			share(CoreAction.CM_SELF_CAST_NOMANA);
			return;
		}
		
		
		if (selfMsg.mTarget == Target.SELF) {
			// self influence to self
			onMessageToSelf(selfMsg);
		} else {
			// self influence to enemy
			// tell enemy : target is he
			selfMsg.mTarget = Target.SELF;
			sendFightMessage(selfMsg);
			share(CoreAction.CM_MANA_CHANGED);
		}
		
		if(mData.selfShape != Shape.NONE) {
			//turn data to enemy logic
			FightMessage castMsg = new FightMessage(Target.ENEMY, CoreAction.CM_ENEMY_CAST);
			castMsg.mParam = mData.selfShape.ordinal(); 
			sendFightMessage(castMsg); //SEND SHAPE TO USER FOR DRAWING
			Log.e("Wizard Fight", "()()()()()()()()()()()(()()( send fight message with shape: " + mData.selfShape);
			share(CoreAction.CM_SELF_CAST_SUCCESS);
		}
		
		// enemy has killed us by buff
		if(mSelfState.getHealth() == 0) {
			finishFight(Target.ENEMY);
		}
	}

	private void onEnemyFightMessage(FightMessage enemyMsg) {
		if (FightActivity.D) Log.e("Wizard Fight", "onEnemyFightMessage [FightCore]");
		
		mData.enemyShape = FightMessage.getShapeFromMessage(enemyMsg);
		Log.e("Wizard Fight", "++++++++++++++ __ENEMY CAST SHAPE: " + mData.enemyShape);
		// refresh enemy health and mana (every enemy message contains it)
		mEnemyState.setHealthAndMana(enemyMsg.mHealth, enemyMsg.mMana);

		if (enemyMsg.mTarget == Target.SELF) {
			onMessageToSelf(enemyMsg);
		} else {
			// Enemy influence to himself
			mEnemyState.handleSpell(enemyMsg);

			if (mEnemyState.getRemovedBuff() != null) {
				share(CoreAction.CM_ENEMY_REMOVED_BUFF);
			}

			if (mEnemyState.getAddedBuff() != null) {
				share(CoreAction.CM_ENEMY_NEW_BUFF);
			}
		}

		// refresh enemy
		if (mData.enemyShape != Shape.NONE) {
			Log.e("Wizard Fight", "++++++++++++++ CM ENEMY CAST FOR SHAPE: " + mData.enemyShape);
			share(CoreAction.CM_ENEMY_CAST);
		}
		share(CoreAction.CM_ENEMY_HEALTH_MANA);
		
		// enemy has killed us
		if(mSelfState.getHealth() == 0) {
			finishFight(Target.ENEMY);
		}
	}

	protected void onMessageToSelf(FightMessage fMessage) {

		FightMessage sendMsg;
		// Enemy influence to player
		mSelfState.handleSpell(fMessage);
		if (mSelfState.getHealth() == 0) {
			return;
		}

		Buff added = mSelfState.getAddedBuff();
		Buff removed = mSelfState.getRemovedBuff();
		Buff refreshed = mSelfState.getRefreshedBuff();
		Shape spellShape = mSelfState.getSpellShape();

		if (removed != null) {
			// buff was removed after spell,
			// send message about buff loss to enemy
			sendMsg = new FightMessage(Target.ENEMY, CoreAction.CM_ENEMY_REMOVED_BUFF,
					removed.ordinal());
			sendFightMessage(sendMsg);

			if (mSelfState.isBuffRemovedByEnemy()) {
				Sound.playBuffSound(removed); // TODO move to other
														// place
			}
			share(CoreAction.CM_REMOVED_BUFF);
		}

		if (added != null) {
			// buff added to player after spell (for example
			// DoT, HoT, or shield),
			// send message about enemy buff success
			sendMsg = new FightMessage(Target.ENEMY, CoreAction.CM_ENEMY_NEW_BUFF,
					added.ordinal());
			sendFightMessage(sendMsg);
			share(CoreAction.CM_NEW_BUFF);
		}

		if (added != null || refreshed != null) {
			// send message of the buff tick
			if (added != null)
				refreshed = added;
			FightMessage fm = new FightMessage(Target.SELF,
					CoreAction.CM_SELF_BUFF_TICK, refreshed.ordinal());
			Message buffTickMsg = mHandler.obtainMessage(
					HandlerMessage.HM_FROM_SELF.ordinal(), fm);
			mHandler.sendMessageDelayed(buffTickMsg,
					refreshed.getDuration());
		}

		if (added == null && removed == null) {
			// nothing with buffs => just send self hp and mana to enemy
			sendMsg = new FightMessage(Target.ENEMY,
					CoreAction.CM_ENEMY_HEALTH_MANA, spellShape.ordinal());
			sendFightMessage(sendMsg);
		}
		
		share(CoreAction.CM_HEALTH_CHANGED);
		share(CoreAction.CM_MANA_CHANGED);
	}

	protected void finishFight(Target winner) {
		Log.e("Wizard Fight", "-----------------------------> finish fight [FightCore]");
		
		mAreMessagesBlocked = true;
		mData.winner = winner;
		share(CoreAction.CM_FIGHT_END);
		 // we must inform enemy about loss
		if (winner == Target.ENEMY) {
			mHandler.obtainMessage(HandlerMessage.HM_SELF_DEATH.ordinal())
				.sendToTarget();
		}
	}

	private void share(CoreAction ca) {
		setChanged(); notifyObservers(ca);
	}
	/* GETTERS FOR OBSERVERS */
	public Handler getHandler() {
		return mHandler;
	}

	public ObservableData getData() {
		return mData;
	}
	
	public PlayerState getSelfState() {
		return mSelfState;
	}
	
	public PlayerState getEnemyState() {
		return mEnemyState;
	}
	
	public void release() {
		if(mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
	}
}