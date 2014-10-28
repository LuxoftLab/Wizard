package com.wizardfight.views;

import com.wizardfight.R;
import com.wizardfight.Shape;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/*
 * Image view with fading out
 */
public class SpellPicture extends ImageView {
	private Animation animFadeOut;
	
    public SpellPicture (Context context,AttributeSet attrs) {
        super(context, attrs);
        setImageDrawable(null);
        animFadeOut = AnimationUtils
				.loadAnimation(getContext(), R.anim.fade_out);
		animFadeOut.setFillAfter(true);
    }
    /* 
     * sets shape and starts fading out
     */
    public void setShape(Shape s) {
    	if( s == Shape.NONE ) {
    		this.setImageDrawable(null);
    	} else {
    		this.setImageResource( Shape.getPictureId(s) );
    		startAnimation(animFadeOut);
    	}
    }
    
    public void setPictureAndFade(int id) {
    	this.setImageResource(id);
    	startAnimation(animFadeOut);
    }
}