package com.example.wizard1;

import android.os.Handler;
import com.example.wizard1.components.Vector2d;
import java.util.ArrayList;

/**
 * Created by 350z6_000 on 24.06.2014.
 */
public abstract class Recognition {
    /**
     * �������� ���������.
     */
    private static ArrayList<Vector2d> PC;
    /**
     * ������ ����� ������������ ��� �������. ������������ � ������� {@link #calcAngles()}.
     */
    private static ArrayList<Double> angles;

    /**
     * �������� ������� ������������� ��������.
     *
     * @param projection ��������, ������� ����� ��������������.
     *
     * @return ���������� �������� ������ ��� "-", ���� ������ �� ����������.
     */
    public static Shape recognize(ArrayList<Vector2d> projection) {
        PC = projection;
        ProjectionView pv = new ProjectionView();
        calcAngles();

        ArrayList<ArrayList<Vector2d>> lines = isEnd(30, 5);
        if (isCircle()) {
        	int size=lines.size();
        	if(size>3)
        		for(int i=0;i<size-1;i++)
        			for(int j=i+2;j<size-1;j++)
        				if(isClock(lines.get(i).get(0), lines.get(i+1).get(0), lines.get(j).get(0), lines.get(j+1).get(0)))
        					return Shape.FAIL;
            return Shape.CIRCLE;
        }
        
        pv.clearProjection();
        for (ArrayList<Vector2d> line : lines)
            pv.addProjection(line);
        switch (lines.size()) {
            case 3:
                return Shape.TRIANGLE;
            case 4:
                if (isClock(lines.get(0).get(0),
                        lines.get(1).get(0),
                        lines.get(2).get(0),
                        lines.get(3).get(0)))
                    return Shape.CLOCK;
                return Shape.SQUARE;
        }
        return Shape.FAIL;
    }

    /**
     * ���������� ������ ����� ������������ ��� �������. <b>������������� ����!</b> //todo ����� ����� � #isEnd
     *
     * @see components.Vector2d#AngleLineAndAxis(components.Vector2d, components.Vector2d)
     */
    private static void calcAngles() {
        angles = new ArrayList<Double>();
        for (int i = 1; i < PC.size(); i++) {
            angles.add(Vector2d.AngleLineAndAxis(PC.get(i - 1), PC.get(i)));
        }
    }

    /**
     * ������ ��������� �������� �� ������ ������.
     *
     * @return true - ����. false - �� ����.
     */
    private static boolean isCircle() {
        int pNum = angles.size();
        int cut = (int) ((pNum * 0.4) / 2);
        for (int i = cut; i < pNum - cut; ++i) {
            if (Math.abs(angles.get(i) - angles.get(i + 1)) > 30)
                return false;
        }
        return true;
    }


    /**
     * ������ ��������� �������� �� ������ ��������� ������. ����������� ������� ������������ �����.
     *
     * @param A 1� ���� �������� �����.
     * @param B 2� ���� �������� �����.
     * @param C 3� ���� �������� �����.
     * @param D 4� ���� �������� �����.
     *
     * @return true - �������� ����. false - ������ ������.
     */
    private static boolean isClock(Vector2d A, Vector2d B, Vector2d C, Vector2d D) {
        ArrayList<Vector2d> al=new ArrayList<Vector2d>();
        al.add(A);al.add(B);al.add(C);al.add(D);
        for(int i=0;i<2;i++) {
            if (Vector2d.hasIntersection(al.get(0), al.get(1), al.get(2), al.get(3)))
                return true;
            Vector2d temp=al.get(0);
            al.remove(0);
            al.add(temp);
        }
        return false;
    }

    /**
     * ������� ������ �� ������ �������.
     *
     * @param ang  ����(������), ���������� �������� ����� ��������� ���������.
     * @param step ������������ ����� ��������(���������� �����). ���� ������� �� ������� ������ ��� �������������
     *             �����, �� �� ����� ��������� ��� 2 ��� ����� ���������.
     *
     * @return ������ ��������.
     */
    private static ArrayList<ArrayList<Vector2d>> isEnd(int ang, int step) {
        ArrayList<ArrayList<Vector2d>> A = new ArrayList<ArrayList<Vector2d>>();
        int pNum = PC.size();
        int last = 0;
        for (int i = 0; i < pNum; i++) {
            if ((i < step * 3) || (i + step > pNum - 1))
                continue;
            Vector2d pr = PC.get(i - step * 2);
            Vector2d tec = PC.get(i - step);
            Vector2d tstep = PC.get(i);
            double an1 = Vector2d.AngleLineAndAxis(pr, tec);
            double an2 = Vector2d.AngleLineAndAxis(tec, tstep);
            if (!((an2 + ang > an1) && (an2 - ang < an1))) {
                A.add(new ArrayList<Vector2d>(PC.subList(last, i)));
                last = i - 1;
                i += step * 2;
            }
        }
        A.add(new ArrayList<Vector2d>(PC.subList(last, PC.size())));
        return A;
    }
}
