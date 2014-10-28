package com.wizardfight;

import java.util.EnumMap;
import android.util.Log;

class BuffState {
	// last tick timestamp
	public final long tickTime;
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
	private static final boolean D = false;
	int health;
	int mana;
	private final int maxHealth;
	private final int maxMana;
	private final EnumMap <Buff, BuffState> buffs;
	private Shape spellShape;
	private Buff addedBuff;
	private Buff refreshedBuff;
	private Buff removedBuff;
	private boolean buffRemovedByEnemy;
	private final PlayerState enemyState;
	
	public PlayerState(int startHP, int startMana, PlayerState enemy) {
		health = maxHealth = startHP;
		mana = maxMana = startMana;
		buffs = new EnumMap<Buff, BuffState>(Buff.class);
		dropSpellInfluence();
		enemyState = enemy;
	}
	
	void dropSpellInfluence() {
		spellShape = Shape.NONE;
		addedBuff = null;
		refreshedBuff = null;
		removedBuff = null;
		buffRemovedByEnemy = false;
	}
	
	void dealDamage(int damage) {
		if( buffs.containsKey(Buff.HOLY_SHIELD) ) {
			handleBuffTick(Buff.HOLY_SHIELD, false);
			return;
		}
		damage = enemyState.recountDamage(damage);
		if (D) Log.e("Wizard Fight", "deal damage: " + damage);
		setHealth(health - damage);
	}
	
	void heal(int hp) {
		setHealth(health + hp);
	}
	
	public void setHealthAndMana(int hp, int mp) {
		health = hp;
		mana = mp;
	}
	
	int recountDamage(int damage) {
		if (D) Log.e("Wizard Fight", "have V?: " + buffs.containsKey(Buff.CONCENTRATION));
		if(buffs.containsKey(Buff.CONCENTRATION)) {
			damage *= 1.5;
		}
		return damage;
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
				if (D) Log.e("Wizard Fight", "BUFF OFF RECEIVED IN STATE");
				if( message.param < 0 ) break;
				// message parameter is buff index
				Buff tickBuff = Buff.values()[ message.param ];
				// apply player state changes 
				switch( tickBuff ) {
				case WEAKNESS:
					dealDamage(tickBuff.getValue());
					break;
				case CONCENTRATION:
					break;
				case BLESSING:
					heal(tickBuff.getValue());
					break;
				case HOLY_SHIELD:
					break;
				default:
				}
				handleBuffTick(tickBuff, (message.target == Target.SELF));
				break;
				
			case BUFF_OFF:
				Buff delBuff = Buff.values()[ message.param ];
				buffs.remove(delBuff);
				removedBuff = delBuff;
				if (D) Log.e("Wizard Fight", delBuff + "was removed");
				break;
			case NEW_HP_OR_MANA:
				break;
			default:
				//nothing;
		}
	}
	
	/* take player mana for spell. Returns true if spell can be casted */
	public boolean requestSpell(FightMessage message) {
		int manaCost = FightMessage.getShapeFromMessage(message).getManaCost();
		if( mana >= manaCost ) {
			mana -= manaCost;
			return true;
		}
		return false;
	}
	
	void addBuff(Buff buff) {
		BuffState buffState = new BuffState( 
				System.currentTimeMillis(), buff.getTicksCount());
		// if map contains buff, it will be replaced with new time value
		buffs.put(buff, buffState);
		if (D) Log.e("Wizard Fight", "new buff was added: " + buff + " " + buffs.get(buff).tickTime);
		addedBuff = buff;
	}
	
	void handleBuffTick(Buff buff, boolean calledByTimer) {
		if (D) Log.e("Wizard Fight", "handleBuffTick called");
		boolean hasBuffAlready = buffs.containsKey(buff);
		if (D) Log.e("Wizard Fight", "has buff that is removed? : " + hasBuffAlready);
		if(hasBuffAlready) {
			BuffState buffState = buffs.get(buff);
			long timeLeft = System.currentTimeMillis() - buffState.tickTime;
			/*
			 * Checking time left need in case when buff was added few times in a row.
			 * Every buff adding causes BUFF_OFF message, that will be sent after specific time,
			 * and it cannot be denied. With this checking messages BUFF_OFF will be 
			 * rejected for previous buff addings.
			 */
			if( calledByTimer && timeLeft < buff.getDuration() ) {
				if (D) Log.e("Wizard Fight", "not enough time left: " + timeLeft + " vs " + buff.getDuration());
				return;
			}
			
			buffState.ticksLeft--;
			if(buffState.ticksLeft == 0) {
				// last tick => need to fully remove buff
				buffs.remove(buff);
				removedBuff = buff;
				if(!calledByTimer) buffRemovedByEnemy = true;
				if (D) Log.e("Wizard Fight", buff + "was removed from handleBuffTick");
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

	
	void setHealth(int hp) {
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
	public boolean isBuffRemovedByEnemy() { return buffRemovedByEnemy; }
}
