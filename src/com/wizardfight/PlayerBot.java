package com.wizardfight;

import com.wizardfight.WizardFight.AppMessage;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PlayerBot extends Thread {
	// for debugging 
	private final boolean D = true;
	private final static String TAG = "Wizard Fight Bot";
	
	private int mStartHP;
	private int mStartMana;
	private Looper mLooper;
	private PlayerState mSelfState;
	private PlayerState mEnemyState;
	private Handler mMainHandler;
	
	public PlayerBot(int startHP, int startMana, Handler mainHandler) {
		mMainHandler = mainHandler;
		mStartHP = startHP;
		mStartMana = startMana;
		setupApp();
	}
	
	public void setupApp() {
		mEnemyState = new EnemyState(mStartHP, mStartMana, null);
		mSelfState = new PlayerState(mStartHP, mStartMana, mEnemyState);
		// Start mana regeneration
		mHandler.removeMessages(AppMessage.MESSAGE_MANA_REGEN.ordinal());
		mHandler.obtainMessage(AppMessage.MESSAGE_MANA_REGEN.ordinal(), null)
			.sendToTarget();
	}
	
	public void run() {
		Looper.prepare();
		Handler handler = new Handler();
		mLooper = Looper.myLooper();
		Looper.loop();
	}
	
	public Handler getHandler() { return mHandler; }
	
	public void release() {
		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		mSelfState = null;
		mEnemyState = null;
		mMainHandler = null;
		mLooper.quit();
	}
	
	// The Handler that gets information back from the BluetoothChatService
		private Handler mHandler = new Handler() {
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
					if (D) Log.e(TAG, "enemy msg: " + enemyMsg);

					switch (enemyMsg.action) {
					case FIGHT_END:
						finishFight(Target.SELF);
						break;
					default:
						handleEnemyMessage(enemyMsg);
					}
					break;
				case MESSAGE_MANA_REGEN:
					mSelfState.manaTick();
					Message msgManaReg = this.obtainMessage(
							AppMessage.MESSAGE_MANA_REGEN.ordinal(), 0, 0, null);
					this.sendMessageDelayed(msgManaReg, 2000);
					break;
				default:
					if (D) Log.e("Wizard Fight", "Unknown message");
					break;
				}
			}

			private void handleSelfMessage(FightMessage selfMsg) {
				if (D)
					Log.e(TAG, "self msg : " + selfMsg);
				boolean canBeCasted = mSelfState.requestSpell(selfMsg);
				if (!canBeCasted)
					return;

				if (selfMsg.target == Target.SELF) {
					// self influence to self
					handleMessageToSelf(selfMsg);
				} else {
					// self influence to enemy
					// tell enemy : target is he
					selfMsg.target = Target.SELF;
					sendFightMessage(selfMsg);
				}
			}

			private void handleEnemyMessage(FightMessage enemyMsg) {
				// refresh enemy health and mana (every enemy message contains it)
				mEnemyState.setHealthAndMana(enemyMsg.health, enemyMsg.mana);
				
				if (D)
					Log.e(TAG, "enemy msg: " + enemyMsg);
				if (enemyMsg.target == Target.SELF) {
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
				if(mSelfState.health == 0) {
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

		private void finishFight(Target winner) {
			setupApp();

			if (winner != Target.SELF) {
				mHandler.obtainMessage(AppMessage.MESSAGE_SELF_DEATH.ordinal())
					.sendToTarget();
			}
		}
		
		private void sendFightMessage(FightMessage msg) {
			msg.health = mSelfState.getHealth();
			msg.mana = mSelfState.getMana();
			byte[] sendBytes = msg.getBytes();
			mMainHandler.obtainMessage(AppMessage.MESSAGE_FROM_ENEMY.ordinal(), sendBytes)
				.sendToTarget();
		}
}
