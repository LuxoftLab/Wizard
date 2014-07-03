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
class ProjectionView {
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


    public void clearProjection() {
        projection = new ArrayList<ArrayList<Vector2d>>();
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
    }
}
