package xyz.izaak.radon.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Created by ibaker on 17/08/2016.
 */
public class OrthonormalBasis extends Basis {
    public static OrthonormalBasis STANDARD = new OrthonormalBasis(Points.X__, Points._Y_);

    public static Matrix4f rotation(OrthonormalBasis from, OrthonormalBasis to) {
        Matrix4f firstRotation = new Matrix4f();
        Matrix4f secondRotation = new Matrix4f();
        Vector3f axis = new Vector3f();
        Vector3f toJ = new Vector3f();
        float angle;

        axis.set(from.getI()).cross(to.getI());
        angle = (float) Math.acos(from.getI().dot(to.getI()));
        firstRotation.rotation(angle, axis);

        toJ.set(to.getJ());
        firstRotation.transformDirection(toJ);
        angle = (float) Math.acos(from.getJ().dot(toJ));
        secondRotation.rotation(angle, to.getI());

        return secondRotation.mul(firstRotation);
    }

    public OrthonormalBasis(Vector3f i, Vector3f j) {
        super();
        float dotProduct = i.dot(j);
        if (dotProduct != 0) {
            throw new IllegalArgumentException(
                    String.format("Vectors %s and %s are not orthogonal. Their dot product is %f", i, j, dotProduct));
        }
        this.getI().set(i);
        this.getJ().set(j);
        Points.copyOf(this.getI()).cross(j, this.getK());

        this.getMatrix().set(
                this.getI().x, this.getJ().x, this.getK().x,
                this.getI().y, this.getJ().y, this.getK().y,
                this.getI().z, this.getJ().z, this.getK().z
        ).transpose();
    }
}
