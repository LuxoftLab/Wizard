package com.wizardfight.views;

import com.wizardfight.Buff;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/*
 * Active buff indicator
 */
public class BuffPicture extends ImageView {
	private Buff mBuff;
	
	public BuffPicture(Context context,AttributeSet attrs) {
		super(context, attrs);
		setBuff(Buff.NONE);
	}
	
	public void setBuff(Buff b) {
		mBuff = b;
		if( mBuff == Buff.NONE ) {
			this.setImageDrawable(null);
		} else {
			this.setImageResource( Buff.getPictureId(b) );
		}
		invalidate();
	}
	
	public Buff getBuff() {
		return mBuff;
	}
}
