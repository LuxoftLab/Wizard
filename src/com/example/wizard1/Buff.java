package com.example.wizard1;

/* 
 * contains all possible buffs, in development
 */
public enum Buff {
//	D_O_T,
//	H_O_T,
//	SHIELD_TEMP,
//	HIGH_DAMAGE,
//	HIGH_HEAL,
//	LOW_DAMAGE,
//	LOW_HEAL,
	WEAKNESS,
	CONCENTRATION,
	BLESSING,
	HOLY_SHIELD,
	NONE;
	
	public static int getPictureId(Buff b) {
    	switch( b ) {
    	case WEAKNESS:
    		return R.drawable.buff_weakness;
    	case CONCENTRATION:
    		return R.drawable.buff_concentration;
    	case BLESSING:
    		return R.drawable.buff_blessing;
    	case HOLY_SHIELD: 
    		return R.drawable.buff_shield;
    	default:
    		return -1;
    	}
	}
	
	public static Buff getBuffFromShape(Shape s) {
		switch(s) {
		case Z:
			return Buff.WEAKNESS;
		case V:
			return Buff.CONCENTRATION;
		case PI:
			return Buff.BLESSING;
		case SHIELD:
			return Buff.HOLY_SHIELD;
		default:
			return Buff.NONE;
		}
	}
}
