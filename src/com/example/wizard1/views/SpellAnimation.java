package com.example.wizard1.views;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by 350z6_000 on 11.07.2014.
 */
public class SpellAnimation extends View {
    public double maxP = 100;
    protected double progress = 0;
    protected Paint paint = new Paint();
    protected double distanse = 0;
    protected double wb;
    protected double hb;
    protected boolean rotate = false;
    ArrayList<Bitmap> phoneIm = new ArrayList<Bitmap>();
    protected ArrayList<Double[]> trajectory = new ArrayList<Double[]>();

    public SpellAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
    }

    Handler mHandler = new Handler();
    Runnable mTick = new Runnable() {
        public void run() {
            if (progress >= maxP) {
                progress = 0;
                mHandler.removeCallbacks(mTick);
                return;
            }
            progress++;
            invalidate();
            mHandler.postDelayed(this, 20);
        }
    };


    public void setTrajectory(ArrayList<Double[]> trajectory, boolean rotate, boolean round) {
        wb = getWidth() / 10;
        hb = wb / 50 * 93;
        for (int i = 1; i < 6; i++) {
            try {
                Bitmap p = BitmapFactory.decodeStream(getContext().getAssets().open("phone/phone" + i + ".png"));
                phoneIm.add(Bitmap.createScaledBitmap(p, (int) (wb), (int) (hb), false));
            } catch (IOException e) {
            }
        }

        if(round){
            this.trajectory =new ArrayList<Double[]>();
            double b=100;
            double bn = 0;
            for(int i=0;i<trajectory.size();i++)
            {
                if(b <(trajectory.get(i))[1])
                    b=(trajectory.get(i))[1];
                if(bn >(trajectory.get(i))[1])
                    bn=(trajectory.get(i))[1];
            }
            double mh=Math.abs(b-bn);
            mh=100-(100-mh/2);
            for(int i=0;i<2;i++) {
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
            for (int i = 1; ((maxDistanse > coveredDist) && (i < trajectory.size())); i++) {
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
            for (int i = 0; ((maxDistanse > coveredDist) && (i < trajectory.size())); i++) {
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
            canvas.drawBitmap(bm, (float) (tt[0] * w), (float) (tt[1] * h), paint);
            /*Random rand = new Random();
            drawI(tl,(int)(Math.random()*30),(int)(Math.random()*20),canvas,h,w);*/
        }
    }

    protected void drawI(Double[] point, int count, int radius, Canvas canvas, double h, double w) {
        for (int i = 0; i < count; i++) {
            paint.setStrokeWidth((int) (Math.random() * 3) + 1);
            paint.setColor(Color.rgb(255, (int) (Math.random() * 255), (int) (Math.random() * 255)));
            canvas.drawPoint((float) ((point[0] + Math.random() * radius - radius / 2) * h), (float) ((point[1] + Math.random() * radius - radius / 2) * w), paint);
        }
    }

    protected double calcDistanse(double a1, double b1, double a2, double b2) {
        return Math.abs(Math.sqrt(Math.pow(Math.abs(a1 - a2), 2) + Math.pow(Math.abs(b1 - b2), 2)));
    }
}
