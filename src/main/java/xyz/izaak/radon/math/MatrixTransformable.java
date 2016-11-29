package xyz.izaak.radon.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Created by ibaker on 27/08/2016.
*/
public class MatrixTransformable implements Transformable {
    private Matrix4f model = new Matrix4f();
    private Matrix4f scratch = new Matrix4f();
    private Vector4f scratchVector = new Vector4f();

    @Override
    public void scale(float x, float y, float z) {
        model.set(scratch.scaling(x, y, z).mul(model));
    }

    @Override
    public void translate(float x, float y, float z) {
        model.set(scratch.translation(x, y, z).mul(model));
    }

    @Override
    public void rotate(float amount, float x, float y, float z) {
        model.set(scratch.rotation(amount, x, y, z).mul(model));
    }

    public void rotateSelf(float amount, Vector3f axis) {
        rotateSelf(amount, axis.x, axis.y, axis.z);
    }

    public void rotateSelf(float amount, float x, float y, float z) {
        scratchVector.set(Points.homogeneousPoint(Points.ORIGIN_3D));
        model.transform(scratchVector);

    }

    @Override
    public void setTransform(Matrix4f transform) {
        model.set(transform);
    }

    @Override
    public void clearTransforms() {
        model.identity();
    }

    public Matrix4f getModel() {
        return model;
    }
}
