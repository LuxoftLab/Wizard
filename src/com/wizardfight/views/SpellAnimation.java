package com.wizardfight.views;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

public class SpellAnimation extends View {
	private final double mMaxP = 100;
	private double mProgress = 0;
	private final Paint mPaint = new Paint();
	private double mDistance = 0;
	private double mWb;
	private double mHb;
	private boolean mRotate = false;
	private final ArrayList<Bitmap> mPhoneIm = new ArrayList<Bitmap>();
	private ArrayList<Double[]> mTrajectory = new ArrayList<Double[]>();

	public SpellAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint.setAntiAlias(true);
	}

	private final Handler mHandler = new Handler();
	private final Runnable mTick = new Runnable() {
		public void run() {
			mProgress++;
			if (mProgress >= mMaxP) {
				mProgress = mMaxP;
				// mHandler.removeCallbacks(mTick);
				// return;
			}
			invalidate();
			mHandler.postDelayed(this, 20);
		}
	};

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int size = widthMeasureSpec;
		if (size > heightMeasureSpec)
			size = heightMeasureSpec;
		super.onMeasure(size, size);// Creating a square view
	}

	public void setTrajectory(ArrayList<Double[]> trajectory, boolean rotate,
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
			mTrajectory = new ArrayList<Double[]>();
			double b = 100;
			double bn = 0;
			for (Double[] aTrajectory : trajectory) {
				if (b < aTrajectory[1])
					b = aTrajectory[1];
				if (bn > aTrajectory[1])
					bn = aTrajectory[1];
			}
			double mh = Math.abs(b - bn);
			mh = 100 - (100 - mh / 2);
			for (int i = 0; i < trajectory.size() - 1; i++) {
				double a = (trajectory.get(i))[0];
				double an = (trajectory.get(i + 1))[0];
				if (a < an) {
					for (; a <= an; a++) {
						b = mh - Math.sqrt(2500 - Math.pow(50 - a, 2));
						mTrajectory.add(new Double[] { a, b });
					}
				} else {
					for (; a >= an; a--) {
						b = Math.sqrt(2500 - Math.pow(50 - a, 2)) + mh;
						mTrajectory.add(new Double[] { a, b });
					}
				}
			}
		} else {
			mTrajectory = trajectory;
		}
		mDistance = 0;
		for (int i = 1; i < trajectory.size(); i++) {
			Double[] tl = trajectory.get(i - 1);
			Double[] t = trajectory.get(i);
			mDistance += calcDistanse(tl[0], tl[1], t[0], t[1]);
		}
		mRotate = rotate;

	}

	public void startAnimation() {
		mHandler.removeCallbacks(mTick);
		mProgress = 0;
		mHandler.post(mTick);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mTrajectory.size() > 0) {

			double w = (getWidth() - mWb) / 100;
			double h = (getHeight() - mHb) / 100;

			mPaint.setColor(Color.argb(240, 114, 17, 0));
			mPaint.setStrokeWidth(3);
			double maxDistanse;
			maxDistanse = mDistance * (mProgress / mMaxP);
			double coveredDist = 0;
			double curDist;
			Double[] tl = mTrajectory.get(0).clone();
			Double[] t;
			double td = mDistance / 4;
			int g = 0;
			Bitmap bm = mPhoneIm.get(g);
			double wb2 = mWb / 2;
			double hb2 = mHb / 2;

			int i;

			for (i = 1; ((maxDistanse > coveredDist) && (i < mTrajectory.size())); i++) {
				t = mTrajectory.get(i).clone();
				curDist = calcDistanse(tl[0], tl[1], t[0], t[1]);
				if (maxDistanse < coveredDist + curDist) {
					double d1 = maxDistanse - coveredDist;
					t[0] = tl[0] + (t[0] - tl[0]) * d1 / curDist;
					t[1] = tl[1] + (t[1] - tl[1]) * d1 / curDist;
					curDist = d1;
				}
				canvas.drawLine((float) (tl[0] * w + wb2),
						(float) (tl[1] * h + hb2), (float) (t[0] * w + wb2),
						(float) (t[1] * h + hb2), mPaint);
				tl = t;
				coveredDist += curDist;
			}
			Double[] tt = tl;
			tl = mTrajectory.get(0).clone();
			coveredDist = 0;
			canvas.drawBitmap(bm, (float) (tl[0] * w), (float) (tl[1] * h),
					mPaint);
			if (mRotate) {
				g++;
				bm = mPhoneIm.get(g);
			}
			if (mTrajectory.size() < 6) {
				for (int j = 0; j < i - 1; j++) {
					t = mTrajectory.get(j).clone();
					canvas.drawBitmap(bm, (float) (t[0] * w),
							(float) (t[1] * h), mPaint);
				}
			} else {
				for (i = 0; ((maxDistanse > coveredDist) && (i < mTrajectory
						.size())); i++) {
					t = mTrajectory.get(i).clone();
					curDist = calcDistanse(tl[0], tl[1], t[0], t[1]);
					if (maxDistanse < coveredDist + curDist) {

						break;
					} else {
						tl = t;
						for (double n = 1; n < 5; n++)
							if ((coveredDist <= (td * n))
									&& ((coveredDist + curDist) >= (td * n))) {
								canvas.drawBitmap(bm, (float) (tl[0] * w),
										(float) (tl[1] * h), mPaint);
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
			canvas.drawBitmap(bm, (float) (tt[0] * w), (float) (tt[1] * h),
					mPaint);
		}
	}

	double calcDistanse(double a1, double b1, double a2, double b2) {
		return Math.abs(Math.sqrt(Math.pow(Math.abs(a1 - a2), 2)
				+ Math.pow(Math.abs(b1 - b2), 2)));
	}
}
