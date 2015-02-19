package com.wizardfight.views;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

/*
 * View that shows animated spell trajectory
 */
public class SpellAnimation extends View {
	private final float mMaxP = 3000;
	private long mProgress;
	private final Paint mPaint = new Paint();
	private float mDistance = 0;
	private float mWb;
	private float mHb;
	private boolean mRotate = false;
	private final ArrayList<Bitmap> mPhoneIm = new ArrayList<Bitmap>();
	private ArrayList<Float[]> mTrajectory = new ArrayList<Float[]>();

	public SpellAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint.setAntiAlias(true);
	}

	private final Handler mHandler = new Handler();
	private final Runnable mTick = new Runnable() {
		public void run() {
			if ((System.currentTimeMillis()-mProgress)>= mMaxP+1000 ) {
				 mHandler.removeCallbacks(mTick);
				 return;
			}
			invalidate();
			mHandler.postDelayed(this, 16);
		}
	};

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int size = widthMeasureSpec;
		if (size > heightMeasureSpec)
			size = heightMeasureSpec;
		super.onMeasure(size, size);// Creating a square view
	}

	public void setTrajectory(ArrayList<Float[]> trajectory, boolean rotate,
			boolean round) {
		mWb = getWidth() / 10;
		mHb = mWb / 50 * 93;
		for (int i = 1; i < 6; i++) {
			try {
				Bitmap p = BitmapFactory.decodeStream(getContext().getAssets()
						.open("phone/phone" + i + ".png"));
				mPhoneIm.add(Bitmap.createScaledBitmap(p, (int) (mWb),
						(int) (mHb), false));
			} catch (IOException e) {
				Log.e("Wizard Fight", "SpellAnimationError", e);
			}
		}

		if (round) {
			mTrajectory = new ArrayList<Float[]>();
			float b = 100;
			float bn = 0;
			for (Float[] aTrajectory : trajectory) {
				if (b < aTrajectory[1])
					b = aTrajectory[1];
				if (bn > aTrajectory[1])
					bn = aTrajectory[1];
			}
			float mh = Math.abs(b - bn);
			mh = 100 - (100 - mh / 2);
			for (int i = 0; i < trajectory.size() - 1; i++) {
				float a = (trajectory.get(i))[0];
				float an = (trajectory.get(i + 1))[0];
				if (a < an) {
					for (; a <= an; a++) {
						b = mh - (float)Math.sqrt(2500 - Math.pow(50 - a, 2));
						mTrajectory.add(new Float[] { a, b });
					}
				} else {
					for (; a >= an; a--) {
						b = (float)Math.sqrt(2500 - Math.pow(50 - a, 2)) + mh;
						mTrajectory.add(new Float[] { a, b });
					}
				}
			}
		} else {
			mTrajectory = trajectory;
		}
		mDistance = 0;
		for (int i = 1; i < trajectory.size(); i++) {
			Float[] tl = trajectory.get(i - 1);
			Float[] t = trajectory.get(i);
			mDistance += calcDistance(tl[0], tl[1], t[0], t[1]);
		}
		mRotate = rotate;

	}

	public void startAnimation() {
		//mHandler.removeCallbacks(mTick);
		mProgress = System.currentTimeMillis();
		invalidate();
		//mHandler.post(mTick);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float kProgress=1;
		if (mTrajectory.size() > 0) {

			float w = (getWidth() - mWb) / 100.0f;
			float h = (getHeight() - mHb) / 100.0f;

			mPaint.setColor(Color.argb(240, 114, 17, 0));
			mPaint.setStrokeWidth(3);
			kProgress=((System.currentTimeMillis()-mProgress)/ mMaxP);
			if(kProgress>1)
				kProgress=1;
			float maxDistanse = mDistance *kProgress;
			float coveredDist = 0;
			float curDist;
			Float[] tl = mTrajectory.get(0).clone();
			Float[] t;
			float td = mDistance / 4;
			int g = 0;
			Bitmap bm = mPhoneIm.get(g);
			float wb2 = mWb / 2;
			float hb2 = mHb / 2;

			int i;
			for (i = 1; ((maxDistanse > coveredDist) && (i < mTrajectory.size())); i++) {
				t = mTrajectory.get(i).clone();
				curDist = calcDistance(tl[0], tl[1], t[0], t[1]);
				if (maxDistanse < coveredDist + curDist) {
					float d1 = maxDistanse - coveredDist;
					t[0] = tl[0] + (t[0] - tl[0]) * d1 / curDist;
					t[1] = tl[1] + (t[1] - tl[1]) * d1 / curDist;
					curDist = d1;
				}
				canvas.drawLine(tl[0] * w + wb2,
						tl[1] * h + hb2, t[0] * w + wb2,
						t[1] * h + hb2, mPaint);
				tl = t;
				coveredDist += curDist;
			}
			Float[] tt = tl;
			tl = mTrajectory.get(0).clone();
			coveredDist = 0;
			canvas.drawBitmap(bm, tl[0] * w, tl[1] * h,
					mPaint);
			if (mRotate) {
				g++;
				bm = mPhoneIm.get(g);
			}
			if (mTrajectory.size() < 6) {
				for (int j = 0; j < i - 1; j++) {
					t = mTrajectory.get(j).clone();
					canvas.drawBitmap(bm, t[0] * w,
							t[1] * h, mPaint);
				}
			} else {
				for (i = 0; ((maxDistanse > coveredDist) && (i < mTrajectory
						.size())); i++) {
					t = mTrajectory.get(i).clone();
					curDist = calcDistance(tl[0], tl[1], t[0], t[1]);
					if (maxDistanse < coveredDist + curDist) {

						break;
					} else {
						tl = t;
						for (float n = 1; n < 5; n++)
							if ((coveredDist <= (td * n))
									&& ((coveredDist + curDist) >= (td * n))) {
								canvas.drawBitmap(bm, tl[0] * w,
										tl[1] * h, mPaint);
								if (mRotate) {
									g++;
									if (g > 4)
										g = 4;
									bm = mPhoneIm.get(g);
								}
							}
						coveredDist += curDist;
					}
				}
			}
			canvas.drawBitmap(bm, (tt[0] * w),(tt[1] * h),mPaint);
		}
		if(kProgress<1)
			invalidate();
	}


	float calcDistance(float a1, float b1, float a2, float b2) {
		return (float)Math.abs(Math.sqrt(Math.pow(Math.abs(a1 - a2), 2)
				+ Math.pow(Math.abs(b1 - b2), 2)));
	}
}
