package com.example.wizard1;

public enum Shape { 
	CIRCLE("circle"), 
	TRIANGLE("triangle"), 
	SHIELD("square"), 
	CLOCK("clock"),
	FAIL("fail");
	
	private final String name;
	
	private Shape(String s) {
		name = s;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static int getPictureId(Shape s) {
    	switch( s ) {
			case CIRCLE: 
				return R.drawable.circle;
			case TRIANGLE: 
				return R.drawable.triangle;
			case SHIELD:
				return R.drawable.square;
			case CLOCK:
				return R.drawable.clock;
			default:
				return R.drawable.fail;
    	}
    }
}
