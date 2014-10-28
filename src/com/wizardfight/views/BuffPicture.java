package com.wizardfight.views;

import com.wizardfight.Buff;
import com.wizardfight.Shape;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/*
 * Square with image describing buff (in future)
 */
class BuffPicture extends ImageView {
	private Buff buff;
	
	public BuffPicture(Context context,AttributeSet attrs) {
		super(context, attrs);
		setBuff(Buff.NONE);
	}
	
	public void setBuff(Buff b) {
		buff = b;
		if( buff == Buff.NONE ) {
			this.setImageDrawable(null);
		} else {
			this.setImageResource( Buff.getPictureId(b) );
		}
		invalidate();
	}
	
	public Buff getBuff() {
		return buff;
	}
}
