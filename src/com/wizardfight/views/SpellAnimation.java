package com.wizardfight.views;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by 350z6_000 on 11.07.2014.
 */
public class SpellAnimation extends View {
    private final double maxP = 100;
    private double progress = 0;
    private final Paint paint = new Paint();
    private double distanse = 0;
    private double wb;
    private double hb;
    private boolean rotate = false;
    private final ArrayList<Bitmap> phoneIm = new ArrayList<Bitmap>();
    private ArrayList<Double[]> trajectory = new ArrayList<Double[]>();

    public SpellAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
    }

    private final Handler mHandler = new Handler();
    private final Runnable mTick = new Runnable() {
        public void run() {
            progress++;
            if (progress >= maxP) {
                progress = maxP;
                //mHandler.removeCallbacks(mTick);
                //return;
            }
            invalidate();
            mHandler.postDelayed(this, 20);
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size=widthMeasureSpec;
        if (size>heightMeasureSpec)
            size=heightMeasureSpec;
        super.onMeasure(size,size);//Creating a square view
    }


    public void setTrajectory(ArrayList<Double[]> trajectory, boolean rotate, boolean round) {
        wb = getWidth() / 10;
        hb = wb / 50 * 93;
        for (int i = 1; i < 6; i++) {
            try {
                Bitmap p = BitmapFactory.decodeStream(getContext().getAssets().open("phone/phone" + i + ".png"));
                phoneIm.add(Bitmap.createScaledBitmap(p, (int) (wb), (int) (hb), false));
            } catch (IOException e) {
                Log.e("Wizard Fight","SpellAnimationError",e);
            }
        }

        if(round){
            this.trajectory =new ArrayList<Double[]>();
            double b=100;
            double bn = 0;
            for (Double[] aTrajectory : trajectory) {
                if (b < aTrajectory[1])
                    b = aTrajectory[1];
                if (bn > aTrajectory[1])
                    bn = aTrajectory[1];
            }
            double mh=Math.abs(b-bn);
            mh=100-(100-mh/2);
            for(int i=0;i<trajectory.size()-1;i++) {
                double a = (trajectory.get(i))[0];
                double an = (trajectory.get(i+1))[0];
                if(a<an) {
                    for (; a <= an; a++) {
                        b =mh - Math.sqrt(2500 - Math.pow(50 - a, 2));
                        this.trajectory.add(new Double[]{a, b});
                    }
                }
                else{
                    for (; a >= an; a--) {
                        b =  Math.sqrt(2500 - Math.pow(50 - a, 2))+mh;
                        this.trajectory.add(new Double[]{a, b});
                    }
                }
            }
        }else{
            this.trajectory = trajectory;
        }
        distanse = 0;
        for (int i = 1; i < trajectory.size(); i++) {
            Double[] tl = trajectory.get(i - 1);
            Double[] t = trajectory.get(i);
            distanse += calcDistanse(tl[0], tl[1], t[0], t[1]);
        }
        this.rotate = rotate;

    }

    public void startAnimation() {
        mHandler.removeCallbacks(mTick);
        progress = 0;
        mHandler.post(mTick);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (trajectory.size() > 0) {

            double w = (getWidth() - wb) / 100;
            double h = (getHeight() - hb) / 100;

            paint.setColor(Color.argb(240,114,17,0));
            paint.setStrokeWidth(3);
            double maxDistanse;
            maxDistanse = distanse * (progress / maxP);
            double coveredDist = 0;
            double curDist;
            Double[] tl = trajectory.get(0).clone();
            Double[] t;
            double td = distanse / 4;
            int g = 0;
            Bitmap bm = phoneIm.get(g);
            double wb2 = wb / 2;
            double hb2 = hb / 2;

            int i;

            for (i = 1; ((maxDistanse > coveredDist) && (i < trajectory.size())); i++) {
                t = trajectory.get(i).clone();
                curDist = calcDistanse(tl[0], tl[1], t[0], t[1]);
                if (maxDistanse < coveredDist + curDist) {
                    double d1 = maxDistanse - coveredDist;
                    t[0] = tl[0] + (t[0] - tl[0]) * d1 / curDist;
                    t[1] = tl[1] + (t[1] - tl[1]) * d1 / curDist;
                    curDist = d1;
                }
                canvas.drawLine((float) (tl[0] * w + wb2), (float) (tl[1] * h + hb2), (float) (t[0] * w + wb2), (float) (t[1] * h + hb2), paint);
                tl = t;
                coveredDist += curDist;
            }
            Double[] tt = tl;
            tl = trajectory.get(0).clone();
            coveredDist = 0;
            canvas.drawBitmap(bm, (float) (tl[0] * w), (float) (tl[1] * h), paint);
            if (rotate) {
                g++;
                bm = phoneIm.get(g);
            }
            if(trajectory.size()<6)
            {
                for (int j=0;j<i-1;j++)
                {
                    t = trajectory.get(j).clone();
                    canvas.drawBitmap(bm, (float) (t[0] * w), (float) (t[1] * h), paint);
                }
            }
            else {
                for (i = 0; ((maxDistanse > coveredDist) && (i < trajectory.size())); i++) {
                    t = trajectory.get(i).clone();
                    curDist = calcDistanse(tl[0], tl[1], t[0], t[1]);
                    if (maxDistanse < coveredDist + curDist) {

                        break;
                    } else {
                        tl = t;
                        for (double n = 1; n < 5; n++)
                            if ((coveredDist <= (td * n)) && ((coveredDist + curDist) >= (td * n))) {
                                canvas.drawBitmap(bm, (float) (tl[0] * w), (float) (tl[1] * h), paint);
                                if (rotate) {
                                    g++;
                                    if (g > 4)
                                        g = 4;
                                    bm = phoneIm.get(g);
                                }
                            }
                        coveredDist += curDist;
                    }
                }
            }
            canvas.drawBitmap(bm, (float) (tt[0] * w), (float) (tt[1] * h), paint);
        }
    }

    double calcDistanse(double a1, double b1, double a2, double b2) {
        return Math.abs(Math.sqrt(Math.pow(Math.abs(a1 - a2), 2) + Math.pow(Math.abs(b1 - b2), 2)));
    }
}
