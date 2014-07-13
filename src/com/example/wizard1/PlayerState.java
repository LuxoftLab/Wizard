package com.example.wizard1;

import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import android.util.Log;

class BuffState {
	// last tick timestamp
	public long tickTime;
	// buff ticks count
	public int ticksLeft;
	
	public BuffState(long time, int ticks) {
		tickTime = time;
		ticksLeft = ticks;
	}
}
/*
 * Describes player state. Contains mana/hp current and max value,
 * set of buffs, and more in future
 */
public class PlayerState {
	protected int health;
	protected int mana;
	protected int maxHealth;
	protected int maxMana;
	protected float damageReceiveMultiplier = 1.0f;
	EnumMap <Buff, BuffState> buffs;
	protected Shape spellShape;
	
	protected Buff addedBuff;
	protected Buff refreshedBuff;
	protected Buff removedBuff;
	
	public PlayerState(int startHP, int startMana) {
		health = maxHealth = startHP;
		mana = maxMana = startMana;
		health = 1; /////////////////////////////////////////////////////////// FOR TESTING, DELETE LATER
		buffs = new EnumMap(Buff.class);
		dropSpellInfluence();
	}
	
	protected void dropSpellInfluence() {
		spellShape = Shape.NONE;
		addedBuff = null;
		refreshedBuff = null;
		removedBuff = null;
	}
	
	public void setHealthAndMana(int hp, int mp) {
		health = hp;
		mana = mp;
	}
	
	protected void dealDamage(int damage) {
		if( buffs.containsKey(Buff.HOLY_SHIELD) ) {
			removeBuff(Buff.HOLY_SHIELD, false);
			return;
		}
		damage = (int) (damage * damageReceiveMultiplier);
		setHealth(health - damage);
	}
	
	protected void heal(int hp) {
		setHealth(health + hp);
	}
	
	public void handleSpell(FightMessage message) {
		dropSpellInfluence();
		spellShape = FightMessage.getShapeFromMessage(message);
		
		switch(message.action) {
			case DAMAGE:
				dealDamage(10);
				break;
				
			case HIGH_DAMAGE:
				dealDamage(30);
				break;
				
			case HEAL:
				if( message.target == Target.SELF ) {
					heal(40);
				} else {
					setHealth( message.param );
				}
				break;
				
			case BUFF_ON:
				if( message.param < 0 ) break;
				// message parameter is buff index
				Buff newBuff = Buff.values()[ message.param ];
				addBuff(newBuff);
				break;
				
			case BUFF_TICK:
				Log.e("Wizard Fight", "BUFF OFF RECEIVED IN STATE");
				if( message.param < 0 ) break;
				// message parameter is buff index
				Buff delBuff = Buff.values()[ message.param ];
				// apply player state changes 
				switch( delBuff ) {
				case WEAKNESS:
					dealDamage(5);
					break;
				case CONCENTRATION:
					break;
				case BLESSING:
					heal(5);
					break;
				case HOLY_SHIELD:
					break;
				default:
				}
				removeBuff(delBuff, (message.target == Target.SELF));
				break;
				
			case NEW_HP_OR_MANA:
				break;
			default:
				//nothing;
		}
	}
	
	/* take player mana for spell. Returns true if spell can be casted */
	public boolean requestSpell(FightMessage message) {
		int manaCost = 0;

		switch( message.action ) {
		case DAMAGE:
			manaCost = 5;
			break;
		case HIGH_DAMAGE:
			manaCost = 15;
			break;
		case HEAL:
			manaCost = 20;
			break;
		case BUFF_ON:
			Buff buff = Buff.values()[ message.param ];
			switch(buff) {
			case WEAKNESS:
				manaCost = 10;
				break;
			case CONCENTRATION:
				manaCost = 15;
				break;
			case BLESSING:
				manaCost = 15;
				break;
			case HOLY_SHIELD:
				manaCost = 10;
				break;
			default:
				//nothing
			}
			break;
		default:
			//nothing
		}
		if( mana >= manaCost ) {
			mana -= manaCost;
			return true;
		}
		return false;
	}
	
	public void addBuff(Buff buff) {
		// save adding date and ticks count
		BuffState buffState = new BuffState(
				new Date().getTime(), buff.getTicksCount());
		// if map contains buff, it will be replaced with new time value
		buffs.put(buff, buffState);
		Log.e("Wizard Fight", "new buff was added: " + buff + " " + buffs.get(buff).tickTime);
		addedBuff = buff;
		refreshedBuff = buff;
	}
	
	public void removeBuff(Buff buff, boolean calledByTimer) {
		Log.e("Wizard Fight", "removeBuff called");
		boolean hasBuffAlready = buffs.containsKey(buff);
		Log.e("Wizard Fight", "has buff that is removed? : " + hasBuffAlready);
		if(hasBuffAlready) {
			BuffState buffState = buffs.get(buff);
			long timeLeft = new Date().getTime() - buffState.tickTime;
			/*
			 * Checking time left need in case when buff was added few times in a row.
			 * Every buff adding causes BUFF_OFF message, that will be sent after specific time,
			 * and it cannot be denied. With this checking messages BUFF_OFF will be 
			 * rejected for previous buff addings.
			 */
			if( calledByTimer && timeLeft < buff.getDuration() ) {
				Log.e("Wizard Fight", "not enough time left: " + timeLeft + " vs " + buff.getDuration());
				return;
			}
			
			buffState.ticksLeft--;
			if(buffState.ticksLeft == 0) {
				// last tick => need to fully remove buff
				buffs.remove(buff);
				removedBuff = buff;
				Log.e("Wizard Fight", "buf was removed");
			} else {
				// not last tick => say that buff is refreshed
				refreshedBuff = buff;
			}
		}
	}
	
	public void manaTick() {
		mana += 5;
		if(mana > maxMana) mana = maxMana;
	}
	
	protected void setMana(int mp) {
		mana = mp;
		if(mana < 0) mana = 0;
		if(mana > maxMana) mana = maxMana;
	}
	
	protected void setHealth(int hp) {
		health = hp;
		if(health < 0) health = 0;
		if(health > maxHealth) health = maxHealth;
	}
	
	public Shape getSpellShape() { return spellShape; }
	public Buff getAddedBuff() { return addedBuff; }
	public Buff getRefreshedBuff() { return refreshedBuff; }
	public Buff getRemovedBuff() { return removedBuff; }
	public int getHealth() { return health; }
	public int getMana() { return mana; }
}
