package com.wizardfight.views;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/*
 * Horizontal bar with current and maximum value
 * and specified color
 */
public class HealthIndicator extends Indicator {
    protected int underBarColor;
    protected float prevValue;
    private double animStep;
    long mAnimStartTime;

    Handler mHandler = new Handler();
    Runnable mTick = new Runnable() {
        public void run() {
            if(prevValue != curValue)
            {
                if(prevValue > curValue)
                    prevValue -= animStep;
                if (prevValue < curValue)
                    prevValue = curValue;
                invalidate();
                mHandler.postDelayed(this, 20);
            }
            else
                mHandler.removeCallbacks(mTick);
        }
    };
    void startAnimation() {
        mAnimStartTime = SystemClock.uptimeMillis();
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }
	
    public HealthIndicator (Context context,AttributeSet attrs) {
        super(context, attrs);
        barColor = Color.GREEN;
		textColor = Color.BLACK;
        underBarColor = Color.RED;
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        rect.set(0,0,width,height);

        Path clipPath = new Path();
        float radius = 10.0f;
        float padding = radius / 2;
        int w = this.getWidth();
        int h = this.getHeight();
        clipPath.addRoundRect(new RectF(padding, padding, w - padding, h - padding), radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        paint.setColor(Color.GRAY);
        canvas.drawRect(rect, paint);

        paint.setColor(underBarColor);
        rect.right = width * prevValue / maxValue;
        canvas.drawRect(rect, paint);

        paint.setColor(barColor);
        rect.right = width * curValue / maxValue;
        canvas.drawRect(rect, paint);
        
        String label = curValue + "/" + maxValue;
        paint.setTextSize(height * 0.3f);
        paint.getTextBounds(label, 0, label.length(), textBounds);
        paint.setColor(textColor);
        
        canvas.drawText(label, (width - textBounds.width()) * 0.5f, 
        		(height + textBounds.height()) * 0.5f, paint);
    }
    
    public void setValue(int value) {
        if(value>maxValue)
            value=maxValue;
        else if(value<0)
            value=0;
    	curValue = value;
    	if(curValue < prevValue) { // damage 
    		animStep = (prevValue - curValue) * 0.1f;
        	startAnimation();
    	} else {
    		prevValue = curValue;
    	}
    }
    
    public void setMaxValue(int value) {
    	curValue = maxValue = value;
    }
    
    public int getValue() {
    	return curValue;
    }
}