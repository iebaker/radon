package xyz.izaak.radon.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by ibaker on 17/08/2016.
 */
public abstract class Camera {

    public static final int YAW = 0;
    public static final int PITCH_LIMIT = 1;
    public static final int HEIGHT_ANGLE = 2;
    public static final int NEAR_PLANE = 3;
    public static final int FAR_PLANE = 4;
    public static final int PITCH = 5;
    public static final int ASPECT_RATIO = 6;
    public static final int FOV = 7;

    private Vector3f eye;
    private Vector3f eyePlusLook;
    private Vector3f look;
    private Vector3f heading;
    private Vector3f right;
    private Vector3f up;

    private Matrix4f view;
    private Matrix4f projection;

    private float[] parameters;

    public Camera() {
        this.eye = Points.copyOf(Points.___);
        this.look = Points.copyOf(Points.__Z);
        this.eyePlusLook = Points.copyOf(this.eye);
        parameters = new float[8];
    }

    public Camera(Camera other) {
        for(int i = 0; i < parameters.length; i++) {
            set(i, other.get(i));
        }
    }

    private void checkParameterIndex(String operation, int parameterIndex) {
        if (parameterIndex >= parameters.length) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot %s Camera parameter at index %d (max %d)",
                            operation, parameterIndex, parameters.length));
        }
    }

    private void recomputePitch() {
        parameters[PITCH] = Math.min(parameters[PITCH], parameters[PITCH_LIMIT]);
        parameters[PITCH] = Math.max(parameters[PITCH], -parameters[PITCH_LIMIT]);
    }

    private void recomputeLook() {
        float x = (float) (Math.cos(parameters[YAW]) * Math.cos(parameters[PITCH]));
        float y = (float) (Math.sin(parameters[PITCH]));
        float z = (float) (Math.sin(parameters[YAW]) * Math.cos(parameters[PITCH]));
        look.set(x, y, z);
    }

    private void recomputeHeading() {
        float x = (float) (Math.cos(parameters[YAW]));
        float y = 0.0f;
        float z = (float) (Math.sin(parameters[YAW]));
        heading.set(x, y, z);
        right.set(heading.z, 0.0f, -heading.x);
    }

    private void recomputeUp() {
        if (parameters[PITCH] < 0) {
            up.set(look.x, 1.0f, look.z).normalize();
        } else {
            up.set(0.0f, 1.0f, 0.0f);
        }
    }

    private void recomputeView() {
        view.setLookAt(eye, eyePlusLook, Points._Y_);
    }

    private void recomputeProjection() {
        projection.setPerspective(
                parameters[FOV],
                parameters[ASPECT_RATIO],
                parameters[NEAR_PLANE],
                parameters[FAR_PLANE]);
    }

    private void recomputeFromParameterIndex(int... parameterIndices) {
        for (int parameterIndex : parameterIndices) {
            switch (parameterIndex) {
                case YAW:
                    recomputeLook();
                    recomputeUp();
                    break;
                case PITCH_LIMIT:
                case PITCH:
                    recomputePitch();
                    recomputeLook();
                    recomputeUp();
                    break;
                case NEAR_PLANE:
                case FAR_PLANE:
                case ASPECT_RATIO:
                case FOV:
                    recomputeProjection();
                    break;
            }
        }
    }

    public float get(int parameterIndex) {
        checkParameterIndex("get", parameterIndex);
        return parameters[parameterIndex];
    }

    public void set(int parameterIndex, float value) {
        checkParameterIndex("set", parameterIndex);
        parameters[parameterIndex] = value;
        recomputeFromParameterIndex(parameterIndex);
    }

    public void add(int parameterIndex, float value) {
        checkParameterIndex("add to", parameterIndex);
        parameters[parameterIndex] += value;
        recomputeFromParameterIndex(parameterIndex);
    }

    public void sub(int parameterIndex, float value) {
        checkParameterIndex("subtract from", parameterIndex);
        parameters[parameterIndex] -= value;
        recomputeFromParameterIndex(parameterIndex);
    }

    public void rotate(float yaw, float pitch) {
        add(PITCH, pitch);
        add(YAW, yaw);
        recomputeFromParameterIndex(PITCH, YAW);
    }

    public void translate(Vector3f amount) {
        eye.add(amount);
        eyePlusLook.set(eye).add(look);
        recomputeView();
    }

    public void translateTo(Vector3f location) {
        eye.set(location);
        eyePlusLook.set(eye).add(look);
        recomputeView();
    }

    public void setLook(Vector3f look) {
        look.normalize();
        set(PITCH, (float) Math.asin(look.y));
        set(YAW, (float) Math.atan2(look.z, look.x));
        recomputeFromParameterIndex(PITCH, YAW);
    }

    public Vector3f getEye() {
        return eye;
    }

    public Vector3f getLook() {
        return look;
    }

    public Vector3f getUp() {
        return up;
    }

    public Vector3f getHeading() {
        return heading;
    }

    public Vector3f getRight() {
        return right;
    }

    public Matrix4f getView() {
        return view;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public void capture(Renderable... renderables) {
        capture(Arrays.asList(renderables));
    }

    public abstract <R extends Renderable> void capture(Collection<R> renderables);
}
