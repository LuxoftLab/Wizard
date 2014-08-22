package com.wizardfight.components;

public class Vector3d extends Vector2d {
	public double z;

    public Vector3d() {
        super();
        z = 0;
    }

    public Vector3d(double X, double Y, double Z) {
        super(X, Y);
        z = Z;
    }

    public double getZ() {
        return z;
    }

    /**
     * Поворот вектора с использованием матрицы поворота
     *
     * @param m матрица поворота(3x3)
     */
    public void rotate(Matrix3x3 m) {
        double _x = (x * m.x11 + y * m.x12 + z * m.x13);
        double _y = (x * m.x21 + y * m.x22 + z * m.x23);
        double _z = (x * m.x31 + y * m.x32 + z * m.x33);
        x = _x;
        y = _y;
        z = _z;
    }

    /**
     * Вычитание вектора.
     *
     * @param v1 вектор, который будем отнимать.
     */
    public void subtract(Vector3d v1) {
        x -= v1.x;
        y -= v1.y;
        z -= v1.z;
    }

    /**
     * Добавление вектора.
     *
     * @param v1 вектор, который будем добавлять.
     */
    public void add(Vector3d v1) {
        x += v1.x;
        y += v1.y;
        z += v1.z;
    }

    /**
     * Деление вектора на число.
     *
     * @param d делитель.
     */
    public void divide(double d) {
        x /= d;
        y /= d;
        z /= d;
    }

    @Override
    public double getLength(){
        return Math.sqrt(x*x + y*y + z*z);
    }

    @Override
    public String toString() {
        return super.toString() + "; z = " + z;
    }
}
