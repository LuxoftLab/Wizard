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
	SHIELD_ONE_SPELL,
	NONE;
	
	public static int getPictureId(Buff b) {
    	switch( b ) {
			case SHIELD_ONE_SPELL: 
				return R.drawable.buff_shield;
			default:
				return -1;
    	}
	}
}
