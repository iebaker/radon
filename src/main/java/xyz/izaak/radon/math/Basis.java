package xyz.izaak.radon.math;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.lwjgl.system.CallbackI;

/**
 * Created by ibaker on 17/08/2016.
 */
public class Basis {
    private static Matrix3f scratch = new Matrix3f();
    private Matrix3f matrix = new Matrix3f();
    private Vector3f i = new Vector3f();
    private Vector3f j = new Vector3f();
    private Vector3f k = new Vector3f();

    public static void change(Vector3f vector, Basis from, Basis to) {
        scratch.set(to.matrix).invert().mul(from.matrix).transform(vector);
    }

    public Basis() {
        this.i.set(Points.X__);
        this.j.set(Points._Y_);
        this.k.set(Points.__Z);
        this.matrix.set(
                i.x, j.x, k.x,
                i.y, j.y, k.y,
                i.z, j.z, k.z
        ).transpose();
    }

    public Basis(Vector3f i, Vector3f j, Vector3f k) {
        this.i = i;
        this.j = j;
        this.k = k;
        this.matrix.set(
                i.x, j.x, k.x,
                i.y, j.y, k.y,
                i.z, j.z, k.z
        ).transpose();
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
        return matrix;
    }

    @Override
    public String toString() {
        return String.format(
                "i: (%.2f, %.2f, %.2f)%nj: (%.2f, %.2f, %.2f)%nk: (%.2f, %.2f, %.2f)%n",
                i.x, i.y, i.z, j.x, j.y, j.z, k.x, k.y, k.z);
    }
}
