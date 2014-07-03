package com.example.wizard1.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

public class HealthIndicator extends Indicator {
	public HealthIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		barColor = Color.GREEN;
		textColor = Color.WHITE;
	}
}