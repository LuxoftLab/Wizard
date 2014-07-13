package com.example.wizard1;

/* 
 * contains all possible buffs, in development
 */
public enum Buff {
	WEAKNESS(1000L, 5),
	CONCENTRATION(5000L, 1),
	BLESSING(1000L, 5),
	HOLY_SHIELD(5000L, 1),
	NONE(-1L, 0);
	
	private final long duration;
	private final int ticksCount;
	
	private Buff(long durationMillis, int ticks) {
		duration = durationMillis;
		ticksCount = ticks;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public int getTicksCount() { 
		return ticksCount;
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
