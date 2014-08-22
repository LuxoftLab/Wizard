package com.wizardfight.components;

public class Matrix3x3 {
    double x11, x12, x13;
    double x21, x22, x23;
    double x31, x32, x33;

    public Matrix3x3(double X11, double X12, double X13,
                     double X21, double X22, double X23,
                     double X31, double X32, double X33) {
        Init(X11, X12, X13,
                X21, X22, X23,
                X31, X32, X33);

    }

    public Matrix3x3(double ox, double oy, double oz) {
        double ch = Math.cos(oy);//heading
        double sh = Math.sin(oy);
        double ca = Math.cos(oz);//altitude
        double sa = Math.sin(oz);
        double cb = Math.cos(ox);//bank
        double sb = Math.sin(ox);
        Init(ch * ca, sh * sb - ch * sa * cb, ch * sa * sb + sh * cb,
                sa, ca * cb, -ca * sb, -sh * ca, sh * sa * cb + ch * sb, -sh * sa * sb + ch * cb);
    }

    public Matrix3x3() {
        x11 = 0;
        x12 = 0;
        x12 = 0;
        x21 = 0;
        x22 = 0;
        x22 = 0;
        x31 = 0;
        x32 = 0;
        x32 = 0;
    }

    private void Init(double X11, double X12, double X13,
                      double X21, double X22, double X23,
                      double X31, double X32, double X33) {
        x11 = X11;
        x12 = X12;
        x13 = X13;
        x21 = X21;
        x22 = X22;
        x23 = X23;
        x31 = X31;
        x32 = X32;
        x33 = X33;
    }
}
