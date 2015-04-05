package com.wizardfight.views;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wizardfight.R;
import com.wizardfight.fight.FightActivity;

import java.util.ArrayList;

/*
 * Tutorial part that appears at the top
 */
public class WizardDial extends RelativeLayout {
    private final TextView mTextView;
    private final RectButton mNextButton;
    private final Animation mAnimFadeOut;
    private final Animation mAnimFadeIn;
    private ArrayList<WizardDialContent> mContent = new ArrayList<WizardDialContent>();
    private int mCount = -1;
    private final ManaIndicator mManaBar;
    private final HealthIndicator mHealthBar;
    private final RelativeLayout mRelLayout;

    public WizardDial(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        int mPopupId = Integer.MAX_VALUE;
        mTextView.setId(mPopupId);
        mNextButton = new RectButton(context);
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
        params = new LayoutParams(
                LayoutParams.MATCH_PARENT, (int)(176*size)
        );
        mRelLayout.setLayoutParams(params);
        addView(mRelLayout);

        mHealthBar=new HealthIndicator(context,null);
        mRelLayout.addView(mHealthBar);
        mManaBar=new ManaIndicator(context,null);
        mRelLayout.addView(mManaBar);

        ImageView hmb=new ImageView(context);
        hmb.setImageResource(R.drawable.hmbar_m);
        hmb.setAdjustViewBounds(true);
        LayoutParams layoutParams =new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        hmb.setLayoutParams(layoutParams);
        hmb.setId(mPopupId -1);
        mRelLayout.addView(hmb);

        new PlayerGUI(
                mHealthBar,mManaBar,null,hmb,null);

        mAnimFadeOut = AnimationUtils
                .loadAnimation(getContext(), R.anim.fade_out);
        mAnimFadeIn = AnimationUtils
                .loadAnimation(getContext(), R.anim.fade_in);
    }

    public void show() {
        if (getVisibility() == INVISIBLE) {
            setContent(mContent);
            setEnabled(true);
            setVisibility(VISIBLE);
            startAnimation(mAnimFadeIn);
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
                mHealthBar.setMaxValue(FightActivity.PLAYER_HP);
                mManaBar.setMaxValue(FightActivity.PLAYER_MANA);
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
