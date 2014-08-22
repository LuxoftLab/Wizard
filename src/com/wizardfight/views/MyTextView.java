package com.wizardfight.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MyTextView extends TextView {
	private boolean isDraw;
	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setIsDraw(boolean isDraw) {
		int visibility = (isDraw) ? View.VISIBLE : View.INVISIBLE;
		this.setVisibility(visibility);
		this.isDraw = isDraw;
	}
	
	public void onDraw(Canvas canvas) {
		if(isDraw) super.onDraw(canvas);
	}
}