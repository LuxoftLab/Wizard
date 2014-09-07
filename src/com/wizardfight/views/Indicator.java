package com.wizardfight.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/*
 * Horizontal bar with current and maximum value
 * and specified color
 */
public class Indicator extends View {
	protected Paint mPaint;
	protected RectF mRect;
	protected Rect mTextBounds;
	 
	protected int mBarColor;
	protected int mTextColor;
	
	protected int mMaxValue;
	protected int mCurValue;
	
    public Indicator (Context context,AttributeSet attrs) {
        super(context, attrs);
        mBarColor = Color.BLACK;
        mTextColor = Color.WHITE;
        mMaxValue = 100;
        mCurValue = mMaxValue;
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mRect = new RectF();
        mTextBounds = new Rect();
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        mRect.set(0,0,width,height);

        mPaint.setColor(Color.GRAY);
        canvas.drawRect(mRect, mPaint);

        mPaint.setColor(mBarColor);
        mRect.right = width * mCurValue / mMaxValue;
        canvas.drawRect(mRect, mPaint);
        
        String label = mCurValue + "/" + mMaxValue;
        mPaint.setTextSize(height * 0.3f);
        mPaint.getTextBounds(label, 0, label.length(), mTextBounds);

        mPaint.setColor(Color.GRAY);
        canvas.drawText(label, (width - mTextBounds.width()) * 0.5f,
                (height+1+ mTextBounds.height()) * 0.5f, mPaint);

        mPaint.setColor(Color.BLACK);
        canvas.drawText(label, (width - mTextBounds.width()) * 0.5f, 
        		(height + mTextBounds.height()) * 0.5f, mPaint);
    }
    
    public void setValue(int value) {
        if(value>mMaxValue)
            value=mMaxValue;
        else if(value<0)
            value=0;
    	mCurValue = value;
    	invalidate();
    }
    
    public void setMaxValue(int value) {
    	mCurValue = mMaxValue = value;
    	invalidate();
    }
    
    public int getValue() {
    	return mCurValue;
    }
    
    public int getMaxValue() {
    	return mMaxValue;
    }
}