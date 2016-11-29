package xyz.izaak.radon.math;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Created by ibaker on 22/08/2016.
 */
public interface Transformable {
    void scale(float x, float y, float z);
    void translate(float x, float y, float z);
    void rotate(float amount, float x, float y, float z);
    void clearTransforms();
    void setTransform(Matrix4f transform);

    default void scale(float factor) {
        scale(factor, factor, factor);
    }

    default void scale(Vector3f factor) {
        scale(factor.x, factor.y, factor.z);
    }

    default void translate(Vector3f distance) {
        translate(distance.x, distance.y, distance.z);
    }

    default void rotate(float amount, Vector3f axis) {
        rotate(amount, axis.x, axis.y, axis.z);
    }
}
