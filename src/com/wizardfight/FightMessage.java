package com.wizardfight;

import android.util.Log;

enum Target {
	SELF,
	ENEMY;
	
	static String[] names = { "self", "enemy" };
	
	@Override
	public String toString() {
		return names[ this.ordinal() ];
	}
}

/*
 * Fight action type
 */
enum FightAction {
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
	static String[] names = {
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
	
	public static FightAction getFromString(String s) {
		for(int i=0; i<names.length; i++) {
			if( s.equals(names[i]) ) 
				return FightAction.values()[ i ];
		}
		return FightAction.NONE;
	}
}

/*
 * contains all needed info that is transfered between devices
 */
public class FightMessage {
	public Target target;
	public FightAction action;
	public int param;
	public int health;
	public int mana;
	
	public FightMessage(Target tar, FightAction act) {
		target = tar;
		action = act;
		param = -1;
	}
	
	public FightMessage(Target tar, FightAction act, int parameter) {
		target = tar;
		action = act;
		param = parameter;
	}
	
	public FightMessage(Shape shape) {
		param = -1;
		
		switch(shape) {
		case TRIANGLE:
		case CIRCLE:
			target = Target.ENEMY;
			break;
		case CLOCK:
			target = Target.SELF;
			param = Buff.getBuffFromShape(shape).ordinal();
			break;
		case Z:
			target = Target.ENEMY;
			param = Buff.getBuffFromShape(shape).ordinal();
			break;
		case V:
			target = Target.SELF;
			param = Buff.getBuffFromShape(shape).ordinal();
			break;
		case PI:
			target = Target.SELF;
			param = Buff.getBuffFromShape(shape).ordinal();
			break;
		case SHIELD:
			target = Target.SELF;
			param = Buff.getBuffFromShape(shape).ordinal();
			break;
		default:
			target = Target.SELF;
			break;
		}
		action = getActionFromShape(shape);
		
	}
	
	public FightMessage(int targetIndex, int actionIndex, int parameter) {
		target = Target.values()[ targetIndex ];
		action = FightAction.values() [ actionIndex ];
		param = parameter;
	}
	
	public FightMessage(int targetIndex, int actionIndex, int parameter, int hp, int mp) {
		this(targetIndex, actionIndex, parameter);
		health = hp;
		mana = mp;
	}
	
	public static Shape getShapeFromMessage(FightMessage message) {
		Shape shape = Shape.NONE;
		switch( message.action ) {
		case HIGH_DAMAGE:
			shape = Shape.CIRCLE;
			break;
		case DAMAGE:
			shape = Shape.TRIANGLE;
			break;
		case BUFF_ON:
			Buff buff = Buff.values()[ message.param  ];
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
			if(message.param >= 0)
				shape = Shape.values()[ message.param ];
			break;
		default:
			shape = Shape.NONE;
		}
		return shape;
	}
	
	public static FightAction getActionFromShape(Shape shape) {
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
		switch(msg.action) {
		case BUFF_ON:
			Buff buff = Buff.values()[ msg.param ];
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
			Shape s = Shape.values()[ msg.param ];
			FightAction a = FightMessage.getActionFromShape(s);
			spellDealsDamage = ( a != FightAction.HEAL);
			break;
		case NONE:
		default:
			break;
		}
		
		Log.e("Wizard Fight", "sdd: " + spellDealsDamage + ", tar: " + msg.target);
		return (spellDealsDamage != (msg.target == Target.ENEMY));
	}
	
	public void setParam(int parameter) {
		param = parameter;
	}

	public static FightMessage fromBytes(byte[] bytes) {
		if(bytes.length < 8) { 
			Log.e("Wizard fight", "BYTE ARR LENGTH " + bytes.length);
			return null;
		}
		int targetIndex = (int)bytes[0];
		int actionIndex = (int)bytes[1];
		int par = ((int)bytes[2] << 8) | ((int)bytes[3] & 0xFF);
		int hp = ((int)bytes[4] << 8) | ((int)bytes[5] & 0xFF);
		int mp = ((int)bytes[6] << 8) | ((int)bytes[7] & 0xFF);
		return new FightMessage(targetIndex, actionIndex, par, hp, mp);
	}
	
	public byte[] getBytes() {
		byte[] b = new byte[8];
		b[0] = (byte) target.ordinal();
		b[1] = (byte) action.ordinal();
		b[2] = (byte) ((param >> 8) & 0xFF); //high byte
		b[3] = (byte) (param & 0xFF);        //low byte
		
		b[4] = (byte) ((health >> 8) & 0xFF); //high byte
		b[5] = (byte) (health & 0xFF);        //low byte
		
		b[6] = (byte) ((mana >> 8) & 0xFF); //high byte
		b[7] = (byte) (mana & 0xFF);        //low byte
		return b;
	}
	
	@Override 
	public String toString() {
		return target + " " + action + " " + param + " " + health + " " + mana;
	}
}
