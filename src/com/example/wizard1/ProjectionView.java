package com.example.wizard1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.example.wizard1.components.Vector2d;
import java.util.ArrayList;

/**
 * Created by 350z6_000 on 23.06.2014.
 */
class ProjectionView extends View {
    /**
     * �������� ���������.
     */
    private ArrayList<ArrayList<Vector2d>> projection;
    /**
     * ������������ ������ ��������. ����� ��� ����������� ��������� ������� ����������. �������������� � {@link
     * #addProjection(java.util.ArrayList)}.
     */
    private double kh, kw;
    /**
     * ���������� �������� x � y ���������. ����� ��� ����������� ����������� ��������� � �����������. �������������� �
     * {@link #addProjection(java.util.ArrayList)}.
     */
    private double minh, minw;

    public ProjectionView (Context context,AttributeSet attrs) {
        super(context,attrs);
        clearProjection();
    }

    public void clearProjection() {
        projection = new ArrayList<ArrayList<Vector2d>>();
        //invalidate();
        //repaint();
    }

    public void setProjection(ArrayList<Vector2d> projection) {
        clearProjection();
        addProjection(projection);
    }

    public void addProjection(ArrayList<Vector2d> projection) {
        this.projection.add(projection);
        double maxh = projection.get(0).getX();
        minh = projection.get(0).getX();
        double maxw = projection.get(0).getY();
        minw = projection.get(0).getY();
        for (ArrayList<Vector2d> pr : this.projection)
            for (Vector2d aProjection : pr) {
                if (aProjection.getX() > maxh)
                    maxh = aProjection.getX();
                if (aProjection.getX() < minh)
                    minh = aProjection.getX();
                if (aProjection.getY() > maxw)
                    maxw = aProjection.getY();
                if (aProjection.getY() < minw)
                    minw = aProjection.getY();
            }
        kh = maxh - minh;
        kw = maxw - minw;
        invalidate();
        //repaint();
    }
    @Override
    public void onDraw(Canvas canvas) {
        Paint paint=new Paint();        
        paint.setColor(Color.YELLOW);
        Paint paintDot=new Paint();
        paintDot.setColor(Color.GREEN);
        paintDot.setStrokeWidth(5);
        if (projection.isEmpty()) {
            canvas.drawLine(0, 0, getWidth(), getHeight(), paint);
            canvas.drawLine(getWidth(), 0, 0, getHeight(),paint);
        } else {
            paint.setStrokeWidth(3);
            double kkh = (getWidth() - 1) / kh;
            double kkw = (getHeight() - 1) / kw;
            Vector2d last = null;
            for (ArrayList<Vector2d> aProjections : projection) {
//                if ((projection.size() == 3) || (projection.size() == 4)) {
//                    canvas.drawLine(
//                            (int) ((aProjections.get(0).getX() - minh) * kkh),
//                            (int) ((aProjections.get(0).getY() - minw) * kkw),
//                            (int) ((aProjections.get(aProjections.size() - 1).getX() - minh) * kkh),
//                            (int) ((aProjections.get(aProjections.size() - 1).getY() - minw) * kkw),paint);
//                } else {
                    for (int n=0;n<aProjections.size(); n++) {
                        Vector2d pD = aProjections.get(n);
                        pD = new Vector2d((pD.getX() - minh) * kkh, (pD.getY() - minw) * kkw);
                        if (last != null)
                            canvas.drawLine(
                                    (int) last.getX(),
                                    (int) last.getY(),
                                    (int) pD.getX(),
                                    (int) pD.getY(),paint);
                        if(n>aProjections.size()-4)
                        	canvas.drawPoint(
                                    (int) pD.getX(),
                                    (int) pD.getY(),paintDot);
                        last = pD;
                    }
//                }
                paint.setColor(Color.rgb((int) (Math.random() * 150) + 50, (int) (Math.random() * 150) + 50, (int) (Math.random() * 150) + 50));
            }
        }
    }
}