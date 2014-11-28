package com.wizardfight;

/* 
 * all game buffs
 */
public enum Buff {
	WEAKNESS(1000L, 5, 5),
	CONCENTRATION(5000L, 1, 0),
	BLESSING(1000L, 5, 5),
	HOLY_SHIELD(5000L, 1, 0),
	NONE(-1L, 0, 0);
	
	private final long mDuration;
	private final int mTicksCount;
	private final int mTickValue;
	
	private Buff(long durationMillis, int ticks, int value) {
		mDuration = durationMillis;
		mTicksCount = ticks;
		mTickValue = value;
	}
	
	public long getDuration() {
		return mDuration;
	}
	
	public int getTicksCount() { 
		return mTicksCount;
	}
	
	public int getValue() {
		return mTickValue;
	}
	
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
