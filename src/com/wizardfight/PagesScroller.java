package com.wizardfight;

import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/*
 * horizontal scroller with stopping on 
 * one of its same-width childs (used in spellbook)
 */
public class PagesScroller extends HorizontalScrollView {
	private boolean mFlag = false;
	private int mSelectedIndex = 0;

	public PagesScroller(android.content.Context context,
			android.util.AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (mFlag) {
			int childCount = getChildCount();
			int size = getWidth() / childCount;
			mFlag = false;
			int s = mSelectedIndex * size;
			int x = getScrollX();
			if (x < s) {
				mSelectedIndex--;
			} else if(x > s) {
				mSelectedIndex++;
			}
			smoothScrollTo(mSelectedIndex * size, 0);
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			mFlag = true;
		}
		return !mFlag || super.dispatchTouchEvent(ev);
	}
}
