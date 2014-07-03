package com.example.wizard1;

import java.util.HashSet;
import java.util.Set;

import android.util.Log;

/*
 * Describes player state. Contains mana/hp current and max value,
 * set of buffs, and more in future
 */
public class PlayerState {
	protected int health;
	protected int mana;
	protected int maxHP;
	protected int maxMana;
	protected Buff canceledBuff;
	protected boolean healthChanged;
	protected boolean manaChanged;
	Set <Buff> buffs;
	
	public PlayerState(int startHP, int startMana) {
		health = maxHP = startHP;
		mana = maxMana = startMana;
		buffs = new HashSet<Buff>();
	}
	
	public void handleSpell(FightMessage message) {
		canceledBuff = null;
		int prevMana = mana;
		int prevHealth = health;
		
		switch(message.action) {
			case HIGH_DAMAGE:
				Log.e("Wizard Fight", "have shield : " + buffs.contains(Buff.SHIELD_ONE_SPELL) );
				if( buffs.contains(Buff.SHIELD_ONE_SPELL) ) {
					removeBuff(Buff.SHIELD_ONE_SPELL);
					return;
				}
				health -= 30;
				break;
			case DAMAGE:
				Log.e("Wizard Fight", "have shield : " + buffs.contains(Buff.SHIELD_ONE_SPELL) );
				if( buffs.contains(Buff.SHIELD_ONE_SPELL) ) {
					removeBuff(Buff.SHIELD_ONE_SPELL);
					return;
				}
				health -= 10;
				break;
			case BUFF_ON:
				if( message.param < 0 ) break;
				//message parameter is buff index
				Buff newBuff = Buff.values()[ message.param ];
				addBuff(newBuff);
				Log.e("Wizard Fight", "buff added to state?: " + buffs.contains(Buff.SHIELD_ONE_SPELL));
				break;
			case BUFF_OFF:
				if( message.param < 0 ) break;
				//message parameter is buff index
				Buff delBuff = Buff.values()[ message.param ];
				removeBuff(delBuff);
				Log.e("Wizard Fight", "buff removed from state?" + !buffs.contains(Buff.SHIELD_ONE_SPELL));
				break;
			case HEAL:
				 /* 
				  * need to fix in future... no param means that message from self
				  */
				if( message.param == -1 ) {
					health += 40;
				} else {
					/* 
					 * other value means that hp specified, so its enemy healing value
					 */
					health = message.param;
				}
				break;
			default:
				//nothing;
		}
		
		if(health < 0) health = 0;
		if(health > maxHP) health = maxHP;
		if(mana < 0) mana = 0; 
		if(mana > maxMana) mana = maxMana;
		
		manaChanged = (mana != prevMana);
		healthChanged = (health != prevHealth);
	}
	
	public void addBuff(Buff buff) {
		buffs.add(buff);
	}
	
	public void removeBuff(Buff buff) {
		if(buffs.contains(buff)) 
			buffs.remove(buff);
		canceledBuff = buff;
	}
	
	public Buff getCanceledBuff() { return canceledBuff; }
	public boolean isManaChanged() { return manaChanged; }
	public boolean isHealthChanged() { return healthChanged; }
	
	public int getHealth() { return health; }
	public int getMana() { return mana; }
}
