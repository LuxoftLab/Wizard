package com.example.wizard1;

import com.example.wizard1.components.*;
import java.util.ArrayList;

/**
 * Класс, который содержит функции для расчетов траектории на основе показаний акселерометра. Created by 350z6_000 on
 * 23.06.14.
 */
abstract class PathCalculator {

    /**
     * Все показаний акселерометра.
     */
    private static ArrayList<Vector4d> records;
    /**
     * Траектория. Вычисляется в {@link #calcData()}.
     */
    private static ArrayList<Vector3d> points;
    /**
     * Калибровочное ускорение. Вычисляется в {@link #calibration(int)}.
     */
    private static Vector4d initialRecord;

    /**
     * Функция запуска расчета траектрии на основе показаний акселерометра.
     *
     * @param acc значения акселерометра
     *
     * @return Возвращает траекторию в виде массивая 3d-точек.
     *
     * @throws Exception Количество кадров калибровки не может быть больше, чем общее количество кадров.
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
     * Функция для калибровки аккселерометра. Рассчитывается среднее ускорение. Калибровочные кадры удоляются из общего
     * списка ({@link #records}).
     *
     * @param countCalibrationFrames количество кадров калибровки.
     *
     * @throws Exception Количество кадров калибровки не может быть больше, чем общее количество кадров.
     */
    private static void calibration(int countCalibrationFrames) throws Exception {
        initialRecord = records.get(countCalibrationFrames - 1);//Первым сохраняем последний кадр, из него берется время.
        records.remove(countCalibrationFrames - 1);
        if (countCalibrationFrames >= records.size())
            throw new Exception("Данных с акселерометра недостаточно " +
                    "для определения траектории или сильно много калибровочных кадров.");
        for (int i = 0; i < countCalibrationFrames - 1; i++) {
            initialRecord.add(records.get(0));
            records.remove(0);
        }
        initialRecord.divide(countCalibrationFrames);
    }

    /**
     * Функция для расчета траектории
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
            cur.subtract(initialRecord);//Отнимаем калибровочное ускорение
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
     * Функция поворота для преобразования траектории в 2d-рисунок
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
     * Функция вписывает проекция в квадрат(100x100), растягивая ее по ширине или высоте, если это необходимо.
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
