package com.wizardfight.views;

import com.wizardfight.R;
import com.wizardfight.Shape;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
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
    }
    
    /* 
     * sets shape and starts fade in + fade out animation sequence
     */
    public void setShape(Shape s) {
    	shape = s;
    	clearAnimation();
    	if( shape == Shape.NONE ) {
    		this.setImageDrawable(null);
//    		invalidate();
    		return;
    	}
        this.setImageResource( Shape.getPictureId(s) );
//        invalidate();
        startAnimation(animFadeOut);
    }
    
    public void setPictureAndFade(int id) {
    	this.setImageResource(id);
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