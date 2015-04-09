package com.wizardfight.achievements;

import android.util.Log;

import com.wizardfight.fight.FightCore;
import com.wizardfight.fight.FightMessage.Target;
import com.wizardfight.fight.FightActivity;
import com.wizardfight.fight.FightCore.CoreAction;

import java.util.Observable;
import java.util.Observer;

/* test class for gathering combat info till onDestroy */
public class AchievementTest implements Observer {
	public static final boolean D = true;
	
	private int mLastHP = FightActivity.PLAYER_HP;
	private int mLastMP = FightActivity.PLAYER_MANA;
	private int mLastEnemyHP = FightActivity.PLAYER_HP;
	
	private int mShieldBlocks;
	private int mWins;
	private int mLosses;
	
	private long mRestoredHealth;
	private long mWastedMana;
	private long mReceivedDamage;
	private long mCausedDamage;
	
	public AchievementTest() {
		
	}
	@Override
	public void update(Observable o, Object action) {
		CoreAction ca = (CoreAction) action;
		FightCore c = (FightCore) o;
		
		switch(ca) {
		case CM_BT_STATE_CHANGE:
			break;
		case CM_CONNECTION_FAIL:
			break;
		case CM_COUNTDOWN_END:
			break;
		case CM_DEVICE_NAME:
			break;
		case CM_ENEMY_CAST:
			break;
		case CM_ENEMY_HEALTH_MANA:
			int enemyHP = c.getEnemyState().getHealth();
			if(enemyHP < mLastEnemyHP) {
				mCausedDamage += mLastEnemyHP - enemyHP;
			}
			mLastEnemyHP = enemyHP;
			break;
		case CM_ENEMY_NEW_BUFF:
			break;
		case CM_ENEMY_READY:
			break;
		case CM_ENEMY_REMOVED_BUFF:
			break;
		case CM_FIGHT_END:
			mLastHP = FightActivity.PLAYER_HP;
			mLastMP = FightActivity.PLAYER_MANA;
			Target winner = c.getData().getWinner();
			Log.e("Wizard Fight","[STAT] WINNER: " + winner);
			if(winner == Target.SELF) {
				mWins++;
			} else {
				mLosses++;
			}
			break;
		case CM_FIGHT_START:
			break;
		case CM_SELF_HEALTH_CHANGED:
			int hp = c.getSelfState().getHealth();
			if(hp > mLastHP) {
				mRestoredHealth += hp - mLastHP;
			} else {
				mReceivedDamage += mLastHP - hp;
			}
			mLastHP = hp;
			break;
		case CM_INFO_STRING:
			break;
		case CM_SELF_MANA_CHANGED:
			int mp = c.getSelfState().getHealth();
			if(mp < mLastMP) {
				mWastedMana += mLastMP - mp;
			}
			mLastMP = mp;
			break;
		case CM_MESSAGE_TO_SEND:
			break;
		case CM_SELF_NEW_BUFF:
			break;
		case CM_SELF_REMOVED_BUFF:
			break;
		case CM_SELF_CAST_NOMANA:
			break;
		case CM_SELF_CAST_SUCCESS:
			break;
		case CM_SELF_SHIELD_BLOCK:
			mShieldBlocks++;
			break;
		default:
			break;
		}
	}
	
	public void showData() {
		if(D) {
			Log.e("Wizard Fight","[STAT] wins: " + mWins + ", losses" + mLosses);
			Log.e("Wizard Fight","[STAT] restored hp: " + mRestoredHealth + ", wasted mana: " + mWastedMana);
			Log.e("Wizard Fight","[STAT] received damage: " + mReceivedDamage + ", caused damage: " + mCausedDamage);
			Log.e("Wizard Fight","[STAT] shield blocks: " + mShieldBlocks);
		}
	}
}
