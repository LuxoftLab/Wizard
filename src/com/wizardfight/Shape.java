package com.wizardfight;

/* 
 * Contains all recognizable shapes
 */
public enum Shape {
    TRIANGLE("triangle", 6, 1.38),
    CIRCLE("circle", 20, 0.85),
    CLOCK("clock", 25, 1.53),
    Z("z", 15, 0.87),
    V("v", 10, 0.69),
    PI("pi", 15, 1.02),
    SHIELD("shield", 10, 0.31),
    FAIL("fail", 0, 0.3),
    NONE("none", 0, 0.3);

    private final String mName;
    private final int mManaCost;
    private final double mCastTime;

    private Shape(String name, int manaCost,double castTime) {
        mName = name;
        mManaCost = manaCost;
        mCastTime = castTime;
    }

    public static int getPictureId(Shape s) {
        switch (s) {
            case TRIANGLE:
                return R.drawable.triangle;
            case CIRCLE:
                return R.drawable.circle;
            case CLOCK:
                return R.drawable.clock;
            case Z:
                return R.drawable.z;
            case V:
                return R.drawable.v;
            case PI:
                return R.drawable.pi;
            case SHIELD:
                return R.drawable.shield;
            case FAIL:
            	return R.drawable.fail;
            default:
                return R.drawable.fail;
        }
    }

    public static Shape getShapeFromString(String str) {
        for (Shape s : Shape.values()) {
            if (s.toString().equals(str)) return s;
        }
        return Shape.NONE;
    }

    public int getManaCost() {
        return mManaCost;
    }

    public double getCastTime() {
        return mCastTime;
    }

    @Override
    public String toString() {
        return mName;
    }
}
