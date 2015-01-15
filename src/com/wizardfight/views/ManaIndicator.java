package com.wizardfight.views;

import android.graphics.Canvas;
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
	@Override
	public void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();
		mRect.set(0, 0, width, height);
		mPaint.setColor(Color.GRAY);
		canvas.drawRect(mRect, mPaint);

		super.onDraw(canvas);
	}
}
