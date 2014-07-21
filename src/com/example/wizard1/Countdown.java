package com.example.wizard1;

import com.example.wizard1.views.MyTextView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by 350z6_000 on 15.07.2014.
 */
public class Countdown extends Activity {
    private int count = 3;
    private MyTextView text;
    private Animation anim;
    private AnimListener animListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.countdown);
        text = (MyTextView) findViewById(R.id.starting_text);
        text.setVisibility(View.INVISIBLE);
        anim = AnimationUtils.loadAnimation(this, R.anim.countdown);

        animListener = new AnimListener();
        anim.setAnimationListener(animListener);
        anim.setFillAfter(true);
        text.setText(count + "");
        text.startAnimation(anim);
        text.setIsDraw(true);
    }

    public void goOut() {
        text.setVisibility(View.INVISIBLE);
//        Intent intent = this.getIntent();
//        intent.putExtra("SOMETHING", "EXTRAS");
//        setResult(RESULT_OK, intent);
        finish();
    }
    
    class AnimListener implements Animation.AnimationListener {
        public void onAnimationEnd(Animation animation) {
        	if( anim.hasEnded() ) {
        		return;
        	}
            count--;
            if(count == -1)
                goOut();
            else {
            	text.setIsDraw(false);
            	if( count != 0) {
            		text.setText(""+count);
            	} else {
            		text.setText("FIGHT!");
            	}
                text.startAnimation(anim);
            }
        }
        public void onAnimationRepeat(Animation animation) { }
        public void onAnimationStart(Animation animation) { 
        	text.setIsDraw(true);
        }
    }
}