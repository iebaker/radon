package xyz.izaak.radon.math;

import org.joml.Matrix3f;
import org.joml.Vector3f;

/**
 * Created by ibaker on 17/08/2016.
 */
class Basis {
    protected Vector3f i;
    protected Vector3f j;
    protected Vector3f k;

    public static void change(Vector3f vector, Basis from, Basis to) {
        to.getMatrix().invert().mul(from.getMatrix()).transform(vector);
    }

    Basis() {
        this.i = Points.X__;
        this.j = Points._Y_;
        this.k = Points.__Z;
    }

    public Basis(Vector3f i, Vector3f j, Vector3f k) {
        this.i = i;
        this.j = j;
        this.k = k;
    }

    public Vector3f getI() {
        return i;
    }

    public Vector3f getJ() {
        return j;
    }

    public Vector3f getK() {
        return k;
    }

    public Matrix3f getMatrix() {
        return new Matrix3f(
                i.x, j.x, k.x,
                i.y, j.y, k.y,
                i.z, j.z, k.z
        ).transpose();
    }
}
