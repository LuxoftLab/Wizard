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
public class Indicator extends View {
	protected Paint paint;
	protected RectF rect;
	protected Rect textBounds;
	 
	protected int barColor;
	protected int textColor;
	
	protected int maxValue;
	protected int curValue;
	
    public Indicator (Context context,AttributeSet attrs) {
        super(context, attrs);
        barColor = Color.BLACK;
        textColor = Color.WHITE;
        maxValue = 100;
        curValue = maxValue;
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rect = new RectF();
        textBounds = new Rect();
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
/*        clipPath.addRoundRect(new RectF(padding, padding, w - padding, h - padding), radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);*/

        paint.setColor(Color.GRAY);
        canvas.drawRect(rect, paint);

        paint.setColor(barColor);
        rect.right = width * curValue / maxValue;
        canvas.drawRect(rect, paint);
        
        String label = curValue + "/" + maxValue;
        paint.setTextSize(height * 0.3f);
        paint.getTextBounds(label, 0, label.length(), textBounds);

        paint.setColor(Color.GRAY);
        canvas.drawText(label, (width - textBounds.width()) * 0.5f,
                (height+1+ textBounds.height()) * 0.5f, paint);

        paint.setColor(Color.BLACK);
        canvas.drawText(label, (width - textBounds.width()) * 0.5f, 
        		(height + textBounds.height()) * 0.5f, paint);
    }
    
    public void setValue(int value) {
        if(value>maxValue)
            value=maxValue;
        else if(value<0)
            value=0;
    	curValue = value;
    	invalidate();
    }
    
    public void setMaxValue(int value) {
    	curValue = maxValue = value;
    	invalidate();
    }
    
    public int getValue() {
    	return curValue;
    }
}