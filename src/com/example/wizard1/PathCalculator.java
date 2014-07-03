package com.example.wizard1;

import com.example.wizard1.components.*;
import java.util.ArrayList;

/**
 * �����, ������� �������� ������� ��� �������� ���������� �� ������ ��������� �������������. Created by 350z6_000 on
 * 23.06.14.
 */
abstract class PathCalculator {

    /**
     * ��� ��������� �������������.
     */
    private static ArrayList<Vector4d> records;
    /**
     * ����������. ����������� � {@link #calcData()}.
     */
    private static ArrayList<Vector3d> points;
    /**
     * ������������� ���������. ����������� � {@link #calibration(int)}.
     */
    private static Vector4d initialRecord;

    /**
     * ������� ������� ������� ��������� �� ������ ��������� �������������.
     *
     * @param acc �������� �������������
     *
     * @return ���������� ���������� � ���� �������� 3d-�����.
     *
     * @throws Exception ���������� ������ ���������� �� ����� ���� ������, ��� ����� ���������� ������.
     */
    public static ArrayList<Vector2d> calculateTrajectory(ArrayList<Vector4d> acc) throws Exception {
        records = acc;
        calibration(3);
        points = new ArrayList<Vector3d>();
        calcData();
        rotate();
        fitIntoSquare();
        ArrayList<Vector2d> p = new ArrayList<Vector2d>();
        for (Vector3d point : points) p.add(point);
        return p;
    }

    /**
     * ������� ��� ���������� ��������������. �������������� ������� ���������. ������������� ����� ��������� �� ������
     * ������ ({@link #records}).
     *
     * @param countCalibrationFrames ���������� ������ ����������.
     *
     * @throws Exception ���������� ������ ���������� �� ����� ���� ������, ��� ����� ���������� ������.
     */
    private static void calibration(int countCalibrationFrames) throws Exception {
        initialRecord = records.get(countCalibrationFrames - 1);//������ ��������� ��������� ����, �� ���� ������� �����.
        records.remove(countCalibrationFrames - 1);
        if (countCalibrationFrames >= records.size())
            throw new Exception("������ � ������������� ������������ " +
                    "��� ����������� ���������� ��� ������ ����� ������������� ������.");
        for (int i = 0; i < countCalibrationFrames - 1; i++) {
            initialRecord.add(records.get(0));
            records.remove(0);
        }
        initialRecord.divide(countCalibrationFrames);
    }

    /**
     * ������� ��� ������� ����������
     */
    private static void calcData() {
        Vector3d point;
        Vector4d cur;
        LinearMovement wx = new LinearMovement();
        LinearMovement wy = new LinearMovement();
        LinearMovement wz = new LinearMovement();
        double t, ti, dt;
        t = initialRecord.getT();
        for (Vector4d record : records) {
            cur = record;
            ti = cur.getT();
            cur.subtract(initialRecord);//�������� ������������� ���������
            dt = (ti - t);
            wx.calcNextCoord(cur.getX(), dt);
            wy.calcNextCoord(cur.getY(), dt);
            wz.calcNextCoord(cur.getZ(), dt);
            point = new Vector3d(wx.getS(), wy.getS(), wz.getS());
            points.add(point);
            t = ti;
        }
    }

    /**
     * ������� �������� ��� �������������� ���������� � 2d-�������
     */
    private static void rotate() {
        int pNum = points.size();
        Vector3d lastP = points.get(pNum - 1);
        double anB = Math.atan(lastP.getX() / lastP.getY());
        double arg = lastP.getZ() / (Math.cos(anB) * lastP.getY() + Math.sin(anB) * lastP.getX());
        double anA = Math.atan(arg);
        Matrix3x3 m = new Matrix3x3(0, -anA + Math.PI / 2, anB + Math.PI / 2);
        for (Vector3d point : points) point.rotate(m);
    }

    /**
     * ������� ��������� �������� � �������(100x100), ���������� �� �� ������ ��� ������, ���� ��� ����������.
     */
    private static void fitIntoSquare() {
        double maxh = points.get(0).getX();
        double minh = points.get(0).getX();
        double maxw = points.get(0).getY();
        double minw = points.get(0).getY();
        for (Vector3d aProjection : points) {
            if (aProjection.getX() > maxh)
                maxh = aProjection.getX();
            if (aProjection.getX() < minh)
                minh = aProjection.getX();
            if (aProjection.getY() > maxw)
                maxw = aProjection.getY();
            if (aProjection.getY() < minw)
                minw = aProjection.getY();
        }
        double kkh = 100 / (maxh - minh);
        double kkw = 100 / (maxw - minw);
        for (int i = 0; i < points.size(); i++) {
            points.set(i, new Vector3d((points.get(i).getX() - minh) * kkh, (points.get(i).getY() - minw) * kkw, points.get(i).getZ()));
        }
    }
}
