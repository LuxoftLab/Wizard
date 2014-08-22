package com.wizardfight.components;

public class Vector2d {
    public double x;
    public double y;

    public Vector2d() {
        x = y = 0;
    }

    public Vector2d(double X, double Y) {
        x = X;
        y = Y;
    }

    /**
     * Функция расчитывает угол между прямой, проходящей через точки a и b, и осью абсцисс.
     *
     * @param a первая точка.
     * @param b вторая точка.
     *
     * @return угол в градусах.
     */
    public static double AngleLineAndAxis(Vector2d a, Vector2d b) {
        return Math.abs(Math.atan2(a.y - b.y, a.x - b.x) / Math.PI * 180);
    }

    /**
     * Функция проверки пересецений
     * @param A
     * @param B
     * @param C
     * @param D
     * @return
     */
    public static boolean hasIntersection(Vector2d A, Vector2d B, Vector2d C, Vector2d D) {

        double d = (A.getX() - B.getX()) * (C.getY() - D.getY()) - (A.getY() - B.getY()) * (C.getX() - D.getX());
        if (d == 0)
            return false;
        double xi = ((C.getX() - D.getX()) * (A.getX() * B.getY() - A.getY() * B.getX()) - (A.getX() - B.getX()) * (C.getX() * D.getY() - C.getY() * D.getX())) / d;
        if (xi < Math.min(A.getX(), B.getX()) || xi > Math.max(A.getX(), B.getX()))
            return false;
        if (xi < Math.min(C.getX(), D.getX()) || xi > Math.max(C.getX(), D.getX()))
            return false;
        return true;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLength(){
        return Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
    }
    
    @Override
    public String toString() {
        return "x = " + x + "; y = " + y;
    }
}
