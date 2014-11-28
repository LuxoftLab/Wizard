package com.wizardfight.views;

import com.wizardfight.views.Indicator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

/*
 * just indicator with its own color style
 */
public class ManaIndicator extends Indicator {
	public ManaIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		mBarColor = Color.BLUE;
		mTextColor = Color.WHITE;
	}
}
