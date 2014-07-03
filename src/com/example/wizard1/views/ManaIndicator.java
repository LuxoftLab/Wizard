package com.example.wizard1.views;

import com.example.wizard1.views.Indicator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

class ManaIndicator extends Indicator {
	public ManaIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		barColor = Color.BLUE;
		textColor = Color.WHITE;
	}
}
