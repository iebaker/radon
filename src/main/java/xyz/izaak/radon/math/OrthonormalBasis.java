package xyz.izaak.radon.math;

import org.joml.Vector3f;

/**
 * Created by ibaker on 17/08/2016.
 */
public class OrthonormalBasis extends Basis {
    public static OrthonormalBasis STANDARD = new OrthonormalBasis(Points.X__, Points._Y_);

    public OrthonormalBasis(Vector3f i, Vector3f j) {
        super();
        float dotProduct = i.dot(j);
        if (dotProduct != 0) {
            throw new IllegalArgumentException(
                    String.format("Vectors %s and %s are not orthogonal. Their dot product is %f", i, j, dotProduct));
        }
        this.getI().set(i);
        this.getJ().set(j);
        this.getI().cross(j, this.getK());
    }
}
