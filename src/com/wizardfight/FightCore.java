package com.wizardfight;

import java.util.Observable;

import com.wizardfight.FightMessage.FightAction;
import com.wizardfight.FightMessage.Target;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FightCore extends Observable {
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private boolean mAreMessagesBlocked;
	// States of players
	private PlayerState mSelfState;
	private PlayerState mEnemyState;

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

	public ObservableData mData;

	// Message types sent from the BluetoothChatService Handler
	enum HandlerMessage {
		HM_BT_STATE_CHANGE, HM_DEVICE_NAME, HM_TOAST, HM_COUNTDOWN_END, HM_CONNECTION_FAIL, HM_FROM_SELF, HM_SELF_DEATH, HM_FROM_ENEMY, HM_MANA_REGEN
	}

	enum CoreAction {
		CM_BT_STATE_CHANGE, CM_DEVICE_NAME, CM_INFO_STRING, CM_COUNTDOWN_END, CM_CONNECTION_FAIL, 
		CM_MESSAGE_TO_SEND, CM_ENEMY_READY, CM_FIGHT_START, CM_HEALTH_CHANGED, CM_MANA_CHANGED, 
		CM_SELF_CAST_SUCCESS, CM_SELF_CAST_NOMANA, CM_NEW_BUFF, CM_REMOVED_BUFF, CM_ENEMY_CAST,
		CM_ENEMY_HEALTH_MANA, CM_ENEMY_NEW_BUFF, CM_ENEMY_REMOVED_BUFF, CM_FIGHT_END;
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			HandlerMessage appMsg = HandlerMessage.values()[msg.what];

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
		init();
	}

	public void init() {
		// Create players states
		mData = new ObservableData();
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
		FightMessage selfDeath = new FightMessage(Target.ENEMY,
				FightAction.FIGHT_END);
		sendFightMessage(selfDeath);
	}

	private void sendFightMessage(FightMessage msg) {
		msg.mHealth = mSelfState.getHealth();
		msg.mMana = mSelfState.getMana();
		msg.mIsBotMessage = false;
		mData.messageToSend = msg;
		share(CoreAction.CM_MESSAGE_TO_SEND);
	}

	protected void onEnemyMessage(FightMessage enemyMsg) {
		switch (enemyMsg.mAction) {
		case ENEMY_READY:
			handleEnemyReadyMessage();
			break;
		case FIGHT_START:
			startFight();
			break;
		case FIGHT_END:
			finishFight(Target.SELF);
			break;
		default:
			if (mAreMessagesBlocked)
				return;
			handleEnemyFightMessage(enemyMsg);
		}
	}

	protected void handleEnemyReadyMessage() {
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
				FightAction.NEW_HP_OR_MANA, Shape.NONE.ordinal());
		sendFightMessage(fMsg);

		// send next tick after 2 sec
		Message msgManaReg = mHandler.obtainMessage(
				HandlerMessage.HM_MANA_REGEN.ordinal(), 0, 0, null);
		mHandler.sendMessageDelayed(msgManaReg, 2000);

		share(CoreAction.CM_MANA_CHANGED);
	}

	private void onSelfMessage(FightMessage selfMsg) {
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
			handleMessageToSelf(selfMsg);
		} else {
			// self influence to enemy
			// tell enemy : target is he
			selfMsg.mTarget = Target.SELF;
			sendFightMessage(selfMsg);
			share(CoreAction.CM_MANA_CHANGED);
		}

		if(mData.selfShape != Shape.NONE) {
			share(CoreAction.CM_SELF_CAST_SUCCESS);
		}
	}

	private void handleEnemyFightMessage(FightMessage enemyMsg) {
		mData.enemyShape = FightMessage.getShapeFromMessage(enemyMsg);
		// refresh enemy health and mana (every enemy message contains it)
		mEnemyState.setHealthAndMana(enemyMsg.mHealth, enemyMsg.mMana);

		if (enemyMsg.mTarget == Target.SELF) {
			handleMessageToSelf(enemyMsg);
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
		if (FightMessage.isSpellCreatedByEnemy(enemyMsg)) {
			share(CoreAction.CM_ENEMY_CAST);
		}
		share(CoreAction.CM_ENEMY_HEALTH_MANA);
	}

	private void handleMessageToSelf(FightMessage fMessage) {

		FightMessage sendMsg;
		// Enemy influence to player
		mSelfState.handleSpell(fMessage);
		if (mSelfState.getHealth() == 0) {
			finishFight(Target.ENEMY);
			return;
		}

		Buff added = mSelfState.getAddedBuff();
		Buff removed = mSelfState.getRemovedBuff();
		Buff refreshed = mSelfState.getRefreshedBuff();
		Shape spellShape = mSelfState.getSpellShape();

		if (removed != null) {
			// buff was removed after spell,
			// send message about buff loss to enemy
			sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_OFF,
					removed.ordinal());
			sendFightMessage(sendMsg);

			if (mSelfState.isBuffRemovedByEnemy()) {
				FightSound.playBuffSound(removed); // TODO move to other
														// place
			}
			share(CoreAction.CM_REMOVED_BUFF);
		}

		if (added != null) {
			// buff added to player after spell (for example
			// DoT, HoT, or shield),
			// send message about enemy buff success
			sendMsg = new FightMessage(Target.ENEMY, FightAction.BUFF_ON,
					added.ordinal());
			sendFightMessage(sendMsg);
			share(CoreAction.CM_NEW_BUFF);
		}

		if (added != null || refreshed != null) {
			// send message of the buff tick
			if (added != null)
				refreshed = added;
			FightMessage fm = new FightMessage(Target.SELF,
					FightAction.BUFF_TICK, refreshed.ordinal());
			Message buffTickMsg = mHandler.obtainMessage(
					HandlerMessage.HM_FROM_SELF.ordinal(), fm);
			mHandler.sendMessageDelayed(buffTickMsg,
					refreshed.getDuration());
		}

		if (added == null && removed == null) {
			// nothing with buffs => just send self hp and mana to enemy
			sendMsg = new FightMessage(Target.ENEMY,
					FightAction.NEW_HP_OR_MANA, spellShape.ordinal());
			sendFightMessage(sendMsg);
		}
		
		share(CoreAction.CM_HEALTH_CHANGED);
		share(CoreAction.CM_MANA_CHANGED);
	}

	private void finishFight(Target winner) {
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
	
	
	public int getHealth() {
		return mSelfState.getHealth();
	}

	public int getMana() {
		return mSelfState.getMana();
	}

	public Buff getRemovedBuff() {
		return mSelfState.getRemovedBuff();
	}
	
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
		mHandler.removeCallbacksAndMessages(null);
	}
}