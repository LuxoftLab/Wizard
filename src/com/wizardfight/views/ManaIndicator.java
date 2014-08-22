package com.wizardfight.views;

import com.wizardfight.views.Indicator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

public class ManaIndicator extends Indicator {
	public ManaIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		barColor = Color.BLUE;
		textColor = Color.WHITE;
	}
}
