package com.example.wizard1.components;

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
     * ������� ������� � �������������� ������� ��������
     *
     * @param m ������� ��������(3x3)
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
     * ��������� �������.
     *
     * @param v1 ������, ������� ����� ��������.
     */
    public void subtract(Vector3d v1) {
        x -= v1.x;
        y -= v1.y;
        z -= v1.z;
    }

    /**
     * ���������� �������.
     *
     * @param v1 ������, ������� ����� ���������.
     */
    public void add(Vector3d v1) {
        x += v1.x;
        y += v1.y;
        z += v1.z;
    }

    /**
     * ������� ������� �� �����.
     *
     * @param d ��������.
     */
    public void divide(double d) {
        x /= d;
        y /= d;
        z /= d;
    }

    @Override
    public double getSum(){
        return Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
    }

    @Override
    public String toString() {
        return super.toString() + "; z = " + z;
    }
}
