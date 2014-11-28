package com.wizardfight;

import android.util.Log;

/*
 * contains all needed info that is transfered between devices
 */
public class FightMessage {
	public Target mTarget;
	public final FightAction mAction;
	public int mParam;
	public int mHealth;
	public int mMana;
	
	public FightMessage(Target tar, FightAction act) {
		mTarget = tar;
		mAction = act;
		mParam = -1;
	}
	
	public FightMessage(Target tar, FightAction act, int parameter) {
		mTarget = tar;
		mAction = act;
		mParam = parameter;
	}
	
	public FightMessage(Shape shape) {
		mParam = -1;
		
		switch(shape) {
		case TRIANGLE:
		case CIRCLE:
			mTarget = Target.ENEMY;
			break;
		case CLOCK:
			mTarget = Target.SELF;
			mParam = Buff.getBuffFromShape(shape).ordinal();
			break;
		case Z:
			mTarget = Target.ENEMY;
			mParam = Buff.getBuffFromShape(shape).ordinal();
			break;
		case V:
			mTarget = Target.SELF;
			mParam = Buff.getBuffFromShape(shape).ordinal();
			break;
		case PI:
			mTarget = Target.SELF;
			mParam = Buff.getBuffFromShape(shape).ordinal();
			break;
		case SHIELD:
			mTarget = Target.SELF;
			mParam = Buff.getBuffFromShape(shape).ordinal();
			break;
		default:
			mTarget = Target.SELF;
			break;
		}
		mAction = getActionFromShape(shape);
		
	}
	
	private FightMessage(int targetIndex, int actionIndex, int parameter) {
		mTarget = Target.values()[ targetIndex ];
		mAction = FightAction.values() [ actionIndex ];
		mParam = parameter;
	}
	
	private FightMessage(int targetIndex, int actionIndex, int parameter, int hp, int mp) {
		this(targetIndex, actionIndex, parameter);
		mHealth = hp;
		mMana = mp;
	}
	
	public static Shape getShapeFromMessage(FightMessage message) {
		Shape shape = Shape.NONE;
		switch( message.mAction ) {
		case HIGH_DAMAGE:
			shape = Shape.CIRCLE;
			break;
		case DAMAGE:
			shape = Shape.TRIANGLE;
			break;
		case BUFF_ON:
			Buff buff = Buff.values()[ message.mParam  ];
			switch(buff) {
			case WEAKNESS:
				shape = Shape.Z;
				break;
			case CONCENTRATION:
				shape = Shape.V;
				break;
			case BLESSING:
				shape = Shape.PI;
				break;
			case HOLY_SHIELD:
				shape = Shape.SHIELD;
				break;
			default:
				shape = Shape.NONE;
			}
			break;
		case HEAL:
			shape = Shape.CLOCK;
			break;
		case NEW_HP_OR_MANA:
			if(message.mParam >= 0)
				shape = Shape.values()[ message.mParam ];
			break;
		default:
			shape = Shape.NONE;
		}
		return shape;
	}
	
	private static FightAction getActionFromShape(Shape shape) {
		FightAction action;
		switch(shape) {
		case CIRCLE:
			action = FightAction.HIGH_DAMAGE;
			break;
		case TRIANGLE:
			action = FightAction.DAMAGE;
			break;
		case Z:
		case V:
		case PI:
		case SHIELD:
			action = FightAction.BUFF_ON;
			break;
		case CLOCK:
			action = FightAction.HEAL;
			break;
		default:
			action = FightAction.NONE;
		}
		return action;
	}

	public static boolean isSpellCreatedByEnemy(FightMessage msg) {
		boolean spellDealsDamage = true;
		switch(msg.mAction) {
		case BUFF_ON:
			Buff buff = Buff.values()[ msg.mParam ];
			switch(buff) {
			case BLESSING:
			case CONCENTRATION:
			case HOLY_SHIELD:
				spellDealsDamage = false;
				break;
			case WEAKNESS:
			default:
				spellDealsDamage = true;
				break;
			}
			break;
		case DAMAGE:
		case HIGH_DAMAGE:
			spellDealsDamage = true;
			break;
		case HEAL:
			spellDealsDamage = false;
			break;
		case NEW_HP_OR_MANA:
			Shape s = Shape.values()[ msg.mParam ];
			FightAction a = FightMessage.getActionFromShape(s);
			spellDealsDamage = ( a != FightAction.HEAL);
			break;
		case NONE:
		default:
			break;
		}
		
		//Log.e("Wizard Fight", "sdd: " + spellDealsDamage + ", tar: " + msg.target);
		return (spellDealsDamage != (msg.mTarget == Target.ENEMY));
	}

	public static FightMessage fromBytes(byte[] bytes) {
		if(bytes.length < 8) { 
			Log.e("Wizard fight", "BYTE ARR LENGTH " + bytes.length);
			return null;
		}
		int targetIndex = (int)bytes[0];
		int actionIndex = (int)bytes[1];
		int par = ((int)bytes[2] << 8) | ((int)bytes[3] & 0xFF);//todo
		int hp = ((int)bytes[4] << 8) | ((int)bytes[5] & 0xFF);
		int mp = ((int)bytes[6] << 8) | ((int)bytes[7] & 0xFF);
		return new FightMessage(targetIndex, actionIndex, par, hp, mp);
	}
	
	public byte[] getBytes() {
		byte[] b = new byte[8];
		b[0] = (byte) mTarget.ordinal();
		b[1] = (byte) mAction.ordinal();
		b[2] = (byte) ((mParam >> 8) & 0xFF); //high byte
		b[3] = (byte) (mParam & 0xFF);        //low byte
		
		b[4] = (byte) ((mHealth >> 8) & 0xFF); //high byte
		b[5] = (byte) (mHealth & 0xFF);        //low byte
		
		b[6] = (byte) ((mMana >> 8) & 0xFF); //high byte
		b[7] = (byte) (mMana & 0xFF);        //low byte
		return b;
	}
	
	@Override 
	public String toString() {
		return mTarget + " " + mAction + " " + mParam + " " + mHealth + " " + mMana;
	}
	
	/*
	 * target of spell
	 */
	public enum Target {
		SELF,
		ENEMY;
		
		static final String[] names = { "self", "enemy" };
		
		@Override
		public String toString() {
			return names[ this.ordinal() ];
		}
	}

	/*
	 * Fight action type
	 */
	public enum FightAction {
		ENEMY_READY,
		FIGHT_START,
		FIGHT_END,
		DAMAGE,
		HIGH_DAMAGE,
		HEAL,
		BUFF_ON,
		BUFF_TICK,
		BUFF_OFF,
		NEW_HP_OR_MANA,
		NONE;
		
		/*
		 *  strings are needed for debugging or string message 
		 *  sending, may be deleted in future
		 */
		static final String[] names = {
			"enemy ready",
			"fight start",
			"fight end",
			"damage",
			"high_damage",
			"heal",
			"buff_on",
			"buff_tick",
			"buff_off",
			"new_hp_or_mana",
			"none"
		};
		
		@Override
		public String toString() {
			return names[ this.ordinal() ];
		}
	}
}
