package com.wizardfight.fight;

import com.wizardfight.Shape;
import com.wizardfight.fight.FightMessage.Target;

import android.util.Log;

import java.util.EnumMap;

class BuffState {
    // last tick timestamp
    public final long mTickTime;
    // buff ticks count
    public int mTicksLeft;

    public BuffState(long tickTime, int ticksCount) {
        mTickTime = tickTime;
        mTicksLeft = ticksCount;
    }
}

enum FightSpell {
	CONE_OF_COLD {
		@Override
		public void execute(PlayerState state) {
			state.applyConeOfCold();
		}
	},
	CIRCLE_OF_FIRE {
		@Override
		public void execute(PlayerState state) {
			state.applyCircleOfFire();
		}
	},
	HEAL {
		@Override
		public void execute(PlayerState state) {
			state.applyHeal();
		}
	},
	WEAKNESS {
		@Override
		public void execute(PlayerState state) {
			state.applyWeakness();
		}
	},
	CONCENTRATION {
		@Override
		public void execute(PlayerState state) {
			state.applyConcentration();
		}
	},
	BLESSING {
		@Override
		public void execute(PlayerState state) {
			state.applyBlessing();
		}
	},
	HOLY_SHIELD {
		@Override
		public void execute(PlayerState state) {
			state.applyHolyShield();
		}
	};
	
	abstract public void execute(PlayerState state);
}

/*
 * Describes player state. Contains mana/hp current and max value,
 * set of buffs, reference to enemy state (for reading only).
 * Handles spells and stores info about appropriate state changes
 */
public class PlayerState {
    private static final boolean D = true;
    private static final EnumMap<Shape, FightSpell> controls;
    static {
    	controls = new EnumMap<Shape, FightSpell>(Shape.class);
    	controls.put(Shape.TRIANGLE, FightSpell.CONE_OF_COLD);
    	controls.put(Shape.CIRCLE, FightSpell.CIRCLE_OF_FIRE);
    	controls.put(Shape.CLOCK, FightSpell.HEAL);
    	controls.put(Shape.Z, FightSpell.WEAKNESS);
    	controls.put(Shape.V, FightSpell.CONCENTRATION);
    	controls.put(Shape.PI, FightSpell.BLESSING);
    	controls.put(Shape.SHIELD, FightSpell.HOLY_SHIELD);
    }
    
    private final int mMaxHealth;
    private final int mMaxMana;
    private final EnumMap<Buff, BuffState> mBuffs;
    private final PlayerState mEnemyState;
    private int mHealth;
    private int mMana;
    private Shape mSpellShape;
    private Buff mAddedBuff;
    private Buff mRefreshedBuff;
    private Buff mRemovedBuff;
    private boolean mBuffRemovedByEnemy;

    public PlayerState(int startHP, int startMana, PlayerState enemy) {
        mHealth = mMaxHealth = startHP;
        mMana = mMaxMana = startMana;
        mBuffs = new EnumMap<Buff, BuffState>(Buff.class);
        dropSpellInfluence();
        mEnemyState = enemy;
    }
        
    /* take player mana for spell. Returns true if spell can be casted */
    public boolean requestSpell(FightMessage message) {
        int manaCost = FightMessage.getShapeFromMessage(message).getManaCost();
        if (mMana >= manaCost) {
            mMana -= manaCost;
            return true;
        }
        return false;
    }
   
    public void handleSpell(FightMessage message) {
        dropSpellInfluence();
        mSpellShape = FightMessage.getShapeFromMessage(message);
        switch (message.mAction) {
        	case CM_SELF_CAST:
        		Shape s = Shape.values()[ message.mParam ];
        		FightSpell spell = controls.get(s); 
        		if(spell != null) spell.execute(this);
        		break;

            case CM_ENEMY_NEW_BUFF:
            case CM_NEW_BUFF:
                Buff newBuff = Buff.values()[message.mParam];
                addBuff(newBuff);
                break;

            case CM_SELF_BUFF_TICK:
            case CM_ENEMY_BUFF_TICK:
                onBuffTick(message);
                break;

            case CM_REMOVED_BUFF:
            case CM_ENEMY_REMOVED_BUFF:
                Buff delBuff = Buff.values()[message.mParam];
                removeBuff(delBuff);
                break;
            default:
                //nothing;
        }
    }

    public void applyConeOfCold() {
    	dealDamage(10);
    }
    
    public void applyCircleOfFire() {
    	dealDamage(30);
    }
    
    public void applyHeal() {
    	heal(40);
    }
    
    public void applyWeakness() {
    	addBuff(Buff.WEAKNESS);
    }
    
    public void applyConcentration() {
    	addBuff(Buff.CONCENTRATION);
    }
    
    public void applyBlessing() {
    	addBuff(Buff.BLESSING);
    }
    
    public void applyHolyShield() {
    	addBuff(Buff.HOLY_SHIELD);
    }
    
    private void onBuffTick(FightMessage msg) {
    	// message parameter is buff index
        Buff tickBuff = Buff.values()[msg.mParam];
        boolean hasEffect = handleBuffTick(tickBuff, (msg.mTarget == Target.SELF));
        if(!hasEffect) return;
        // apply player state changes
        switch (tickBuff) {
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
    }
    
    private void dropSpellInfluence() {
    	if (D) Log.e("Wizard Fight", "drop spell influence");
        mSpellShape = Shape.NONE;
        mAddedBuff = null;
        mRefreshedBuff = null;
        mRemovedBuff = null;
        mBuffRemovedByEnemy = false;
    }

    private void dealDamage(int damage) {
        if (mBuffs.containsKey(Buff.HOLY_SHIELD)) {
            handleBuffTick(Buff.HOLY_SHIELD, false);
            return;
        }
        damage = mEnemyState.recountDamage(damage);
        if (D) Log.e("Wizard Fight", "deal damage: " + damage);
        setHealth(mHealth - damage);
    }

    private int recountDamage(int damage) {
        if (D) Log.e("Wizard Fight", "have V?: " + mBuffs.containsKey(Buff.CONCENTRATION));
        if (mBuffs.containsKey(Buff.CONCENTRATION)) {
            damage *= 1.5;
        }
        return damage;
    }
    
    private void heal(int hp) {
        setHealth(mHealth + hp);
    }

    private void setHealth(int hp) {
        mHealth = hp;
        if (mHealth < 0) mHealth = 0;
        if (mHealth > mMaxHealth) mHealth = mMaxHealth;
    }
    
    private void addBuff(Buff buff) {
        BuffState buffState = new BuffState(
                System.currentTimeMillis(), buff.getTicksCount());
        // if map contains buff, it will be replaced with new time value
        mBuffs.put(buff, buffState);
        if (D) Log.e("Wizard Fight", "new buff was added: " + buff + " " + mBuffs.get(buff).mTickTime);
        mAddedBuff = buff;
    }

    private void removeBuff(Buff buff) {
    	 mBuffs.remove(buff);
         mRemovedBuff = buff;
         if (D) Log.e("Wizard Fight", buff + "was removed");
    }
    
    private boolean handleBuffTick(Buff buff, boolean calledByTimer) {
        boolean hasBuffAlready = mBuffs.containsKey(buff);
        if(!hasBuffAlready) return false;
        
        BuffState buffState = mBuffs.get(buff);
        long timeLeft = System.currentTimeMillis() - buffState.mTickTime;
        /*
		 * Checking time left need in case when buff was added few times in a row.
		 * Every buff adding causes BUFF_OFF message, that will be sent after specific time,
		 * and it cannot be denied. With this checking messages BUFF_OFF will be 
		 * rejected for previous buff addings.
		 */
        if (calledByTimer && timeLeft < buff.getDuration()) {
        	if (D) Log.e("azaza", "not enough time left: " + timeLeft + " vs " + buff.getDuration());
            return false;
        }

        buffState.mTicksLeft--;
        if (buffState.mTicksLeft == 0) {
            // last tick => need to fully remove buff
            removeBuff(buff);
            if (!calledByTimer) mBuffRemovedByEnemy = true;
            if (D) Log.e("Wizard Fight", buff + "was removed from handleBuffTick");
        } else {
            // not last tick => say that buff is refreshed
            mRefreshedBuff = buff;
        }
        
        return true;
    }

    public void manaTick() {
        mMana += 5;
        if (mMana > mMaxMana) mMana = mMaxMana;
    }

    public void setHealthAndMana(int hp, int mp) {
        mHealth = hp;
        mMana = mp;
    }

    // ************************** GETTERS ************************ 
    public Shape getSpellShape() {
        return mSpellShape;
    }

    public Buff getAddedBuff() {
    	if (D) Log.e("Wizard Fight", "get added buff null?: " + (mAddedBuff == null));
        return mAddedBuff;
    }

    public Buff getRefreshedBuff() {
        return mRefreshedBuff;
    }

    public Buff getRemovedBuff() {
        return mRemovedBuff;
    }

    public int getHealth() {
        return mHealth;
    }

    public int getMana() {
        return mMana;
    }

    public boolean isBuffRemovedByEnemy() {
        return mBuffRemovedByEnemy;
    }

    public boolean hasBuff(Buff buff) {
        return (mBuffs.containsKey(buff));
    }
}
