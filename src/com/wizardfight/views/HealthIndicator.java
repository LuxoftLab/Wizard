package com.wizardfight.views;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;

/*
 * Horizontal bar with current and maximum value
 * and specified color
 */
public class HealthIndicator extends Indicator {
    private final int mUnderBarColor;
    private float mPrevValue;
    private double mAnimStep;
    
    private final Handler mHandler = new Handler();
    private final Runnable mTick = new Runnable() {
        public void run() {
            if(mPrevValue != mCurValue)
            {
                if(mPrevValue > mCurValue)
                    mPrevValue -= mAnimStep;
                if (mPrevValue < mCurValue)
                    mPrevValue = mCurValue;
                invalidate();
                mHandler.postDelayed(this, 20);
            }
            else
                mHandler.removeCallbacks(mTick);
        }
    };
    
    public HealthIndicator (Context context,AttributeSet attrs) {
        super(context, attrs);
        mBarColor = Color.GREEN;
		mTextColor = Color.BLACK;
        mUnderBarColor = Color.RED;
    }
    
    void startAnimation() {
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        mRect.set(0,0,width,height);
        mPaint.setColor(Color.GRAY);
        canvas.drawRect(mRect, mPaint);

        mPaint.setColor(mUnderBarColor);
        mRect.right = width * mPrevValue / mMaxValue;
        canvas.drawRect(mRect, mPaint);

        mPaint.setColor(mBarColor);
        mRect.right = width * mCurValue / mMaxValue;
        mRect.right++;
        canvas.drawRect(mRect, mPaint);
        
        String label = mCurValue + "/" + mMaxValue;
        mPaint.setTextSize(height * 0.3f);
        mPaint.getTextBounds(label, 0, label.length(), mTextBounds);
        mPaint.setColor(Color.WHITE);

        canvas.drawText(label, (width - mTextBounds.width()) * 0.5f,
                (height+1 + mTextBounds.height()) * 0.5f, mPaint);
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
    	if(mCurValue < mPrevValue) { // damage 
    		mAnimStep = (mPrevValue - mCurValue) * 0.1f;
        	startAnimation();
    	} else {
    		mPrevValue = mCurValue;
    		invalidate();
    	}
    }
    
    public void setMaxValue(int value) {
    	mCurValue = mMaxValue = value;
    }
    
    public int getValue() {
    	return mCurValue;
    }
}