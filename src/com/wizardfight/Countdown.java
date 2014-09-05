package com.wizardfight;

import com.wizardfight.views.MyTextView;
import com.wizardfight.WizardFight.AppMessage;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by 350z6_000 on 15.07.2014.
 */
public class Countdown {
	private int NUMBERS_COUNT = 3;
    private int mNumCount;
    private View mRootView;
    private MyTextView mText;
    private Animation mAnim;
    private AnimListener mAnimListener;
    private Handler mHandler;

    public Countdown (Context context, View rootView, Handler h){
    	mHandler = h;
    	mRootView = rootView;
    	mText = (MyTextView) mRootView.findViewById(R.id.starting_text);
        mText.setVisibility(View.INVISIBLE);
        mAnim = AnimationUtils.loadAnimation(context, R.anim.countdown);
        mAnimListener = new AnimListener();
        mAnim.setAnimationListener(mAnimListener);
        mAnim.setFillAfter(true);
    }

    public void startCountdown() {
    	mNumCount = NUMBERS_COUNT;
    	mRootView.setVisibility(View.VISIBLE);
    	// set initial text and start animation
        mText.setText(mNumCount + "");
        mText.startAnimation(mAnim);
        mText.setIsDraw(true);
    }
    
    public void goOut() {
    	mRootView.setVisibility(View.INVISIBLE);
        mHandler.obtainMessage(AppMessage.MESSAGE_COUNTDOWN_END.ordinal()).sendToTarget();
    }
    
//    public void setVisible(boolean isVisible) {
//    	mRootView.setVisibility(
//    			(isVisible)? View.VISIBLE : View.GONE);
//    }
    
    class AnimListener implements Animation.AnimationListener {
        public void onAnimationEnd(Animation animation) {
        	if( mAnim.hasEnded() ) {
        		return;
        	}
            mNumCount--;
            if(mNumCount == -1)
                goOut();
            else {
            	mText.setIsDraw(false);
            	if( mNumCount != 0) {
            		mText.setText(""+mNumCount);
            	} else {
            		mText.setText("FIGHT!");
            	}
                mText.startAnimation(mAnim);
            }
        }
        public void onAnimationRepeat(Animation animation) { }
        public void onAnimationStart(Animation animation) { 
        	mText.setIsDraw(true);
        }
    }
}