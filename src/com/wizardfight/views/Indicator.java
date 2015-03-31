package com.wizardfight.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/*
 * Horizontal bar with current and maximum value
 * and specified color
 */
public class Indicator extends View {
	final Paint mPaint;
	final RectF mRect;
	final Rect mTextBounds;
	 
	int mBarColor;
	int mTextColor;
	
	int mMaxValue;
	int mCurValue;
	
    Indicator(Context context, AttributeSet attrs) {
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


        mPaint.setColor(mBarColor);
        mRect.right = width * mCurValue / mMaxValue;
        canvas.drawRect(mRect, mPaint);

        String label = mCurValue + "/" + mMaxValue;
        mPaint.setTextSize(height * 0.45f);
        mPaint.getTextBounds(label, 0, label.length(), mTextBounds);
        mPaint.setColor(mBarColor);

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeMiter(10);
        Log.e("Wizard Fight", "width: " + width);
        mPaint.setStrokeWidth(2 + height/20);//Kostya!
        canvas.drawText(label, (width - mTextBounds.width()) * 0.5f,
                (height + mTextBounds.height()) * 0.5f, mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeMiter(1);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(mTextColor);
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
}