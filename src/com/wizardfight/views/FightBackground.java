package com.wizardfight.views;

import com.wizardfight.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class FightBackground extends ImageView {
	private boolean mFirstAnim = true;
	private Animation mDarkToBright;
	private Animation mBrightToDark;
	
	public FightBackground (Context context,AttributeSet attrs) {
        super(context, attrs);
        mDarkToBright = AnimationUtils
				.loadAnimation(getContext(), R.anim.dark_to_bright);
        mBrightToDark = AnimationUtils
				.loadAnimation(getContext(), R.anim.bright_to_dark);
		mDarkToBright.setFillAfter(true);
		mBrightToDark.setFillAfter(true);
		setImageResource(R.drawable.space);
		darkenImage();
    }
	
	public void darkenImage() {
		mFirstAnim = true;
		clearAnimation();
		invalidate();
		setAlpha(51);
	}
	
	public void toBright() {
		if (mFirstAnim) {
			mFirstAnim = false;
			setAlpha(255);
		}
		startAnimation(mDarkToBright);
	}
	
	public void toDark() {
		startAnimation(mBrightToDark);
	}
}
