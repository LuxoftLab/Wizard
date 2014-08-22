package com.wizardfight.views;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.wizardfight.R;

import java.util.ArrayList;

/**
 * Created by 350z6_000 on 22.07.2014.
 */
public class WizardDial extends RelativeLayout {
    private TextView textView;
    private CancelButton nextButton;
    private Animation animFadeOut;
    private Animation animFadeIn;
    private ArrayList<String> text = new ArrayList<String>();
    private int count = -1;
    private int pause=-1;

    public WizardDial(Context context) {
        super(context);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        textView = new TextView(context);
        textView.setBackgroundResource(R.drawable.wd3);
        textView.setLayoutParams(params);
        textView.setTextSize(15);
        textView.setTextColor(Color.rgb(92,67,51));
        int id=555;
        textView.setId(id);
        nextButton = new CancelButton(context);
        nextButton.setBackgroundResource(R.drawable.next);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        params = new LayoutParams(
                size, size
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.setMargins(0, 2*size/3, size/2, 0);
        params.addRule(RelativeLayout.ALIGN_TOP,id);
        nextButton.setLayoutParams(params);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });
        addView(textView);
        addView(nextButton);
        setBackgroundColor(Color.argb(200, 0, 0, 0));

        animFadeOut = AnimationUtils
                .loadAnimation(getContext(), R.anim.fade_out_nodelay);
        animFadeIn = AnimationUtils
                .loadAnimation(getContext(), R.anim.fade_in);
        setVisibility(INVISIBLE);
    }

    public void show() {
        if (getVisibility() == INVISIBLE) {
            settext(text);
            setEnabled(true);
            setVisibility(VISIBLE);
            startAnimation(animFadeIn);
        }
    }
    public void showQuick() {
        if (getVisibility() == INVISIBLE) {
            settext(text);
            setEnabled(true);
            setVisibility(VISIBLE);
        }
    }

    public void setText(ArrayList<String> text) {
        pause=-1;
        settext(text);
    }
    private void settext(ArrayList<String> text){
        this.text = text;
        count = -1;
        setCanNext(true);
        goNext();
    }

    public void setPause(int pause) {
        this.pause = pause;
    }

    public void goNext() {
        setCanNext(true);
        count++;
        if (count >= text.size()) {
            ((WizardDialDelegate) getContext()).onWizardDialClose();
            setCanNext(false);
            close();
        } else {
            if(count==pause-1)
                setCanNext(false);
            textView.setText(text.get(count));
        }
    }

    public void close() {
        setEnabled(false);
        setVisibility(INVISIBLE);
        startAnimation(animFadeOut);
    }

    public void setCanNext(boolean canNext) {
        nextButton.setEnabled(canNext);
        nextButton.setClickable(canNext);
    }
    public boolean isOnPause(){
        return !nextButton.isEnabled();
    }
}
