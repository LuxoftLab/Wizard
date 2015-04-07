package com.wizardfight.fight;

import java.io.Serializable;

import com.wizardfight.Shape;
import com.wizardfight.fight.FightCore.CoreAction;

/*
 * contains all needed info that is transfered between devices
 */
public class FightMessage implements Serializable {
	private static final long serialVersionUID = 160794200294L;
	
	public static final int SIZE = 9;
	public Target mTarget;
	public CoreAction mAction;
	public int mParam;
	public int mHealth;
	public int mMana;
	// thing needed for server to define is bot
	public boolean mIsBotMessage;
	
	public FightMessage(Target tar, CoreAction act) {
		mTarget = tar;
		mAction = act;
		mParam = -1;
	}
	
	public FightMessage(Target tar, CoreAction act, int parameter) {
		mTarget = tar;
		mAction = act;
		mParam = parameter;
	}
	
	private static boolean shapeDealsDamage(Shape shape) {
		switch(shape) {
		case TRIANGLE:
		case CIRCLE:
		case Z:
			return true;
		case CLOCK:
		case V:
		case PI:
		case SHIELD:
		case FAIL:
			return false;
		default:
			return false;
		}
	}
	
	public FightMessage(Shape shape) {
		mTarget = shapeDealsDamage(shape) ? Target.ENEMY : Target.SELF;
		mParam = shape.ordinal();
		mAction = CoreAction.CM_SELF_CAST;
	}
	
	private FightMessage(int targetIndex, int actionIndex, int parameter) {
		mTarget = Target.values()[ targetIndex ];
		mAction = CoreAction.values() [ actionIndex ];
		mParam = parameter;
	}
	
	private FightMessage(int targetIndex, int actionIndex, int parameter, int hp, int mp, boolean isBot) {
		this(targetIndex, actionIndex, parameter);
		mHealth = hp;
		mMana = mp;
		mIsBotMessage = isBot;
	}
	
	public static Shape getShapeFromMessage(FightMessage message) {
		Shape shape = Shape.NONE;
		switch( message.mAction ) {
		case CM_SELF_CAST:
		case CM_ENEMY_CAST:
			return Shape.values()[ message.mParam ];
		case CM_NEW_BUFF:
		case CM_ENEMY_NEW_BUFF:
		case CM_HEALTH_CHANGED:
		case CM_MANA_CHANGED:
		case CM_ENEMY_HEALTH_MANA:
			break;
		default:
			shape = Shape.NONE;
		}
		return shape;
	}
	
	public static boolean isSpellCreatedByEnemy(FightMessage msg) {
		boolean spellDealsDamage = true;
		switch(msg.mAction) {
		case CM_SELF_CAST:
		case CM_ENEMY_CAST:
			return true; 
		case CM_NEW_BUFF:
		case CM_ENEMY_NEW_BUFF:
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
		case CM_ENEMY_HEALTH_MANA:
			if (msg.mParam == Shape.CLOCK.ordinal()) { //TODO delete this kostil
				return true; 
			}
		case CM_HEALTH_CHANGED:
		case CM_MANA_CHANGED:
			break;
		default:
			break;
		}
		
		return (spellDealsDamage != (msg.mTarget == Target.ENEMY));
	}

	public static FightMessage fromBytes(byte[] bytes) {
		int c = 0;
		
		int targetIndex = (int)bytes[ c++ ];
		int actionIndex = (int)bytes[ c++ ];
		int par = ((int)bytes[ c++ ] << 8) | ((int)bytes[ c++ ] & 0xFF);//todo
		int hp = ((int)bytes[ c++ ] << 8) | ((int)bytes[ c++ ] & 0xFF);
		int mp = ((int)bytes[ c++ ] << 8) | ((int)bytes[ c++ ] & 0xFF);
		boolean isBot = ( bytes[ c ] == (byte)1 );
		
		return new FightMessage(targetIndex, actionIndex, par, hp, mp, isBot);
	}
	
	public byte[] getBytes() {
		byte[] b = new byte[SIZE];
		int c = 0;
		
		b[ c++ ] = (byte) mTarget.ordinal();
		b[ c++ ] = (byte) mAction.ordinal();
		b[ c++ ] = (byte) ((mParam >> 8) & 0xFF); //high byte
		b[ c++ ] = (byte) (mParam & 0xFF);        //low byte
		
		b[ c++ ] = (byte) ((mHealth >> 8) & 0xFF); //high byte
		b[ c++ ] = (byte) (mHealth & 0xFF);        //low byte
		
		b[ c++ ] = (byte) ((mMana >> 8) & 0xFF); //high byte
		b[ c++ ] = (byte) (mMana & 0xFF);        //low byte
		
		b[ c++ ] = (mIsBotMessage) ? (byte)1 : (byte)0;
		
		return b;
	}
	
	@Override 
	public String toString() {
		return mTarget + " " + mAction + " " + mParam + " " + mHealth + " " + mMana + " " + mIsBotMessage;
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

}
