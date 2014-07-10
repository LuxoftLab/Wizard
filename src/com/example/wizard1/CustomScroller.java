package com.example.wizard1;

import android.os.Handler;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by 350z6_000 on 08.07.2014.
 */
public class CustomScroller extends HorizontalScrollView {
	long lastScrollMillis;
	Handler mHandler = new Handler();
	boolean f = false;
	int size = 0;
	int selected = 0;
	int u = 0;

	public CustomScroller(android.content.Context context,
			android.util.AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (f) {
			int childCount = getChildCount();
			size = getWidth() / childCount;
			f = false;
			int s = selected * size;
			int x = getScrollX();
			if (x < s) {
				selected--;
			} else if(x > s) {
				selected++;
			}
			smoothScrollTo(selected * size, 0);
		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			f = true;
		}
		if (!f)
			return true;
		return super.dispatchTouchEvent(ev);
	}
}
