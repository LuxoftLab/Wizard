package com.wizardfight.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by 350z6_000 on 13.07.2014.
 */
public class CancelButton extends Button {

    public CancelButton(Context context) {
        super(context);
    }

    public CancelButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CancelButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setBackgroundDrawable(Drawable d) {
        SAutoBgButtonBackgroundDrawable layer = new SAutoBgButtonBackgroundDrawable(d);
        super.setBackgroundDrawable(layer);
    }

    class SAutoBgButtonBackgroundDrawable extends LayerDrawable {
        // The color filter to apply when the button is pressed
        final ColorFilter _pressedFilter = new LightingColorFilter(Color.DKGRAY, 1);

        public SAutoBgButtonBackgroundDrawable(Drawable d) {
            super(new Drawable[]{d});
        }

        @Override
        protected boolean onStateChange(int[] states) {
            boolean enabled = false;
            boolean pressed = false;

            for (int state : states) {
                if (state == android.R.attr.state_enabled)
                    enabled = true;
                else if (state == android.R.attr.state_pressed)
                    pressed = true;
            }

            mutate();
            if (pressed) {
                setColorFilter(_pressedFilter);
                setAlpha(255);
            } else if(!enabled)
            {
                setColorFilter(null);
                setAlpha(0);
            }
            else {
                setColorFilter(null);
                setAlpha(255);
            }

            invalidateSelf();

            return super.onStateChange(states);
        }

        @Override
        public boolean isStateful() {
            return true;
        }
    }

}