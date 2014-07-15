package com.example.wizard1.views;

import com.example.wizard1.R;
import com.example.wizard1.Shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;

/*
 * Class of spell picture, contains current shape
 */
public class SpellPicture extends ImageView {
	private Shape shape;
	private Animation animFadeOut;
	private FadeOutListener fadeOutListener;
	
    public SpellPicture (Context context,AttributeSet attrs) {
        super(context, attrs);
        shape = Shape.FAIL;
        setImageDrawable(null);
    }
    
    public void initAnimListener() {
		animFadeOut = AnimationUtils
				.loadAnimation(getContext(), R.anim.fade_out);

		fadeOutListener = new FadeOutListener(this);
	    animFadeOut.setAnimationListener(fadeOutListener);
	    Log.e("Wizard Fight", "animation listener initialized");
    }
    
    /* 
     * sets shape and starts fade in + fade out animation sequence
     */
    public void setShape(Shape s) {
    	shape = s;
    	if( shape == Shape.NONE ) {
    		this.setImageDrawable(null);
    	}
    	Log.e("Wizard Fight", "Starting animation");
        Log.e("Wizard Fight", "Current shape : " + shape);
        this.setImageResource( Shape.getPictureId(s) );
        invalidate();
        startAnimation(animFadeOut);
    }
    
    public Shape getShape() {
    	return shape;
    }
 
    class FadeOutListener implements AnimationListener {
    	private ImageView image;
    	public FadeOutListener(ImageView img) {
    		image = img;
    	}
    	public void onAnimationEnd(Animation animation) {
    		image.setImageDrawable(null);
    	}
        public void onAnimationRepeat(Animation animation) {}
        public void onAnimationStart(Animation animation) {}
    }
}