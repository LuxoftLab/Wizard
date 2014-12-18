package com.wizardfight;

import com.wizardfight.FightMessage.Target;
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

/*
 * Describes player state. Contains mana/hp current and max value,
 * set of buffs, reference to enemy state (for reading only).
 * Handles spells and stores info about appropriate state changes
 */
public class PlayerState {
    private static final boolean D = false;
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

    void dropSpellInfluence() {
        mSpellShape = Shape.NONE;
        mAddedBuff = null;
        mRefreshedBuff = null;
        mRemovedBuff = null;
        mBuffRemovedByEnemy = false;
    }

    void dealDamage(int damage) {
        if (mBuffs.containsKey(Buff.HOLY_SHIELD)) {
            handleBuffTick(Buff.HOLY_SHIELD, false);
            return;
        }
        damage = mEnemyState.recountDamage(damage);
        if (D) Log.e("Wizard Fight", "deal damage: " + damage);
        setHealth(mHealth - damage);
    }

    void heal(int hp) {
        setHealth(mHealth + hp);
    }

    public void setHealthAndMana(int hp, int mp) {
        mHealth = hp;
        mMana = mp;
    }

    int recountDamage(int damage) {
        if (D) Log.e("Wizard Fight", "have V?: " + mBuffs.containsKey(Buff.CONCENTRATION));
        if (mBuffs.containsKey(Buff.CONCENTRATION)) {
            damage *= 1.5;
        }
        return damage;
    }

    public void handleSpell(FightMessage message) {
        dropSpellInfluence();
        mSpellShape = FightMessage.getShapeFromMessage(message);

        switch (message.mAction) {
            case DAMAGE:
                dealDamage(10);
                break;

            case HIGH_DAMAGE:
                dealDamage(30);
                break;

            case HEAL:
                if (message.mTarget == Target.SELF) {
                    heal(40);
                } else {
                    setHealth(message.mParam);
                }
                break;

            case BUFF_ON:
                if (message.mParam < 0) break;
                // message parameter is buff index
                Buff newBuff = Buff.values()[message.mParam];
                addBuff(newBuff);
                break;

            case BUFF_TICK:
                if (message.mParam < 0) break;
                // message parameter is buff index
                Buff tickBuff = Buff.values()[message.mParam];
                boolean hasEffect = handleBuffTick(tickBuff, (message.mTarget == Target.SELF));
                if(!hasEffect) break;
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
                
                break;

            case BUFF_OFF:
                Buff delBuff = Buff.values()[message.mParam];
                mBuffs.remove(delBuff);
                mRemovedBuff = delBuff;
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
        if (mMana >= manaCost) {
            mMana -= manaCost;
            return true;
        }
        return false;
    }

    void addBuff(Buff buff) {
        BuffState buffState = new BuffState(
                System.currentTimeMillis(), buff.getTicksCount());
        // if map contains buff, it will be replaced with new time value
        mBuffs.put(buff, buffState);
        if (D) Log.e("Wizard Fight", "new buff was added: " + buff + " " + mBuffs.get(buff).mTickTime);
        mAddedBuff = buff;
    }

    private boolean handleBuffTick(Buff buff, boolean calledByTimer) {
        if (D) Log.e("Wizard Fight", "handleBuffTick called");
        boolean hasBuffAlready = mBuffs.containsKey(buff);
        
        if (D) Log.e("Wizard Fight", "has buff that is removed? : " + hasBuffAlready);
        
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
            Log.e("azaza", "not enough time left: " + timeLeft + " vs " + buff.getDuration());
            return false;
        }

        buffState.mTicksLeft--;
        if (buffState.mTicksLeft == 0) {
            // last tick => need to fully remove buff
            mBuffs.remove(buff);
            mRemovedBuff = buff;
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

    public Shape getSpellShape() {
        return mSpellShape;
    }

    public Buff getAddedBuff() {
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

    void setHealth(int hp) {
        mHealth = hp;
        if (mHealth < 0) mHealth = 0;
        if (mHealth > mMaxHealth) mHealth = mMaxHealth;
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
