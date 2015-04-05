package com.wizardfight.fight;

import com.wizardfight.R;
import com.wizardfight.fight.FightCore.HandlerMessage;
import com.wizardfight.views.HideableTextView;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/*
 * Manages countdown of its text view: 3.. 2.. 1.. FIGHT!
 */
class Countdown {
	private final int NUMBERS_COUNT = 3;
    private int mNumCount;
    private final View mRootView;
    private final HideableTextView mText;
    private final Animation mAnim;
    private final Handler mUiHandler;

    public Countdown (Context context, View rootView, Handler uiHandler){
    	mUiHandler = uiHandler;
    	mRootView = rootView;
    	mText = (HideableTextView) mRootView.findViewById(R.id.starting_text);
        mText.setVisibility(View.INVISIBLE);
        mAnim = AnimationUtils.loadAnimation(context, R.anim.countdown);
        AnimListener animListener = new AnimListener();
        mAnim.setAnimationListener(animListener);
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
    
    void goOut() {
    	mRootView.setVisibility(View.INVISIBLE);
        mUiHandler.obtainMessage(HandlerMessage.HM_COUNTDOWN_END.ordinal()).sendToTarget();
    }

    private class AnimListener implements Animation.AnimationListener {
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
            		mText.setText(R.string.countdown_fight);
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