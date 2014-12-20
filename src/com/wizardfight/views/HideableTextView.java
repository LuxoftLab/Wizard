package com.wizardfight.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/*
 * Text view with switching between
 * fully visible/invisible states
 */
public class HideableTextView extends TextView {
	private boolean mIsDraw;
	public HideableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setIsDraw(boolean isDraw) {
		int visibility = (isDraw) ? View.VISIBLE : View.INVISIBLE;
		this.setVisibility(visibility);
		this.mIsDraw = isDraw;
	}
	
	public void onDraw(Canvas canvas) {
		if(mIsDraw) super.onDraw(canvas);
	}
}