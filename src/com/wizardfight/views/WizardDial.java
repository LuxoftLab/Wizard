package com.wizardfight.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.wizardfight.R;

import java.util.ArrayList;

/*
 * Tutorial part that appears at the top
 */
public class WizardDial extends RelativeLayout {
    private final TextView mTextView;
    private final CancelButton mNextButton;
    private final Animation mAnimFadeOut;
    private final Animation mAnimFadeIn;
    private ArrayList<WizardDialContent> mContent = new ArrayList<WizardDialContent>();
    private int mCount = -1;
    private final ManaIndicator mManaBar;
    private final HealthIndicator mHealthBar;
    private final RelativeLayout mRelLayout;
    private final int mPopupId = Integer.MAX_VALUE;

    public WizardDial(Context context) {
        super(context);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mTextView = new TextView(context);
        mTextView.setBackgroundResource(R.drawable.wd3);
        mTextView.setLayoutParams(params);
        mTextView.setTextSize(metrics.widthPixels/20  / (metrics.xdpi/160));

        mTextView.setTextColor(Color.rgb(92,67,51));
        mTextView.setId(mPopupId);
        mNextButton = new CancelButton(context);
        mNextButton.setBackgroundResource(R.drawable.next);
        double size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        		1, getResources().getDisplayMetrics());
        params = new LayoutParams(
                (int)(size*40), (int)(size*40)
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.setMargins(0,(int)(80*size/3), (int)(20*size), 0);
        params.addRule(RelativeLayout.ALIGN_TOP, mPopupId);
        mNextButton.setLayoutParams(params);
        mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });
        addView(mTextView);
        addView(mNextButton);
        setBackgroundColor(Color.argb(200, 0, 0, 0));


        mRelLayout=new RelativeLayout(context);
        addView(mRelLayout);
        params = new LayoutParams(
                (int)(196*size), (int)(36*size)
        );
        params.setMargins((int)(2*size),(int)(25*size),0,0);
        mHealthBar=new HealthIndicator(context,null);
        mHealthBar.setLayoutParams(params);
        mHealthBar.setValue(100);
        mRelLayout.addView(mHealthBar);
        params = new LayoutParams(
                (int)(196*size), (int)(36*size)
        );
        params.setMargins((int)(2*size),(int)(63*size),0,0);
        mManaBar=new ManaIndicator(context,null);
        mManaBar.setLayoutParams(params);
        mManaBar.setValue(200);
        mRelLayout.addView(mManaBar);
        params = new LayoutParams(
                (int)(310*size), (int)(176*size)
        );
        params.setMargins(0,0,0,0);
        ImageView hmb=new ImageView(context);
        hmb.setImageResource(R.drawable.hmbar_m);
        hmb.setLayoutParams(params);
        mRelLayout.addView(hmb);
        mRelLayout.setVisibility(INVISIBLE);

        mAnimFadeOut = AnimationUtils
                .loadAnimation(getContext(), R.anim.fade_out);
        mAnimFadeIn = AnimationUtils
                .loadAnimation(getContext(), R.anim.fade_in);
        setVisibility(INVISIBLE);
    }

    public void show() {
        if (getVisibility() == INVISIBLE) {
            setContent(mContent);
            setEnabled(true);
            setVisibility(VISIBLE);
            startAnimation(mAnimFadeIn);
        }
    }
    public void showQuick() {
        if (getVisibility() == INVISIBLE) {
            setContent(mContent);
            setEnabled(true);
            setVisibility(VISIBLE);
        }
    }

    public void setContent(ArrayList<WizardDialContent> content){
        this.mContent = content;
        mCount = -1;
        goNext();
    }
    public void goNext() {
        setCanNext(true);
        mCount++;
        mRelLayout.setVisibility(INVISIBLE);
        if (mCount >= mContent.size()) {
            ((WizardDialDelegate) getContext()).onWizardDialClose();
            setCanNext(false);
            close();
        } else {
            mHandler.removeCallbacks(mTick);
            if(mContent.get(mCount).isUi()) {
                mHealthBar.setMaxValue(100);
                mManaBar.setMaxValue(100);
                mRelLayout.setVisibility(VISIBLE);
                mHandler.post(mTick);
            }
            setCanNext(!mContent.get(mCount).isPause());
            mTextView.setText(mContent.get(mCount).getText());
        }
    }
    private final Handler mHandler = new Handler();
    private int health=3;
    private int mana=3;
    private final Runnable mTick = new Runnable() {
        public void run() {
            if(mContent.get(mCount).isHealth())
            {
                if(health%2==1)
                    mHealthBar.setValue(mHealthBar.getValue()-20);
                health++;
                if(health>=6)
                {
                    health=0;
                    mHealthBar.setValue(mHealthBar.getValue()+60);
                }
            }
            if(mContent.get(mCount).isMana())
            {
                mManaBar.setValue(mManaBar.getValue()+5);
                mana++;
                if(mana>=3)
                {
                    mana=0;
                    mManaBar.setValue(mManaBar.getValue()-20);
                }
            }
            mHandler.postDelayed(this, 1000);
        }
    };
    void close() {
        mHandler.removeCallbacks(mTick);
        setEnabled(false);
        setVisibility(INVISIBLE);
        startAnimation(mAnimFadeOut);
    }
    void setCanNext(boolean canNext) {
        mNextButton.setEnabled(canNext);
        mNextButton.setClickable(canNext);
    }
    public boolean isOnPause(){
        return !mNextButton.isEnabled();
    }
}
