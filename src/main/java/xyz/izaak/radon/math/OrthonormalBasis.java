package xyz.izaak.radon.math;

import org.joml.Vector3f;

/**
 * Created by ibaker on 17/08/2016.
 */
public class OrthonormalBasis extends Basis {
    public OrthonormalBasis(Vector3f i, Vector3f j) {
        float dotProduct = i.dot(j);
        if (dotProduct != 0) {
            throw new IllegalArgumentException(
                    String.format("Vectors %s and %s are not orthogonal. Their dot product is %f", i, j, dotProduct));
        }
        this.i.set(i);
        this.j.set(j);
        this.i.cross(j, this.k);
    }
}
