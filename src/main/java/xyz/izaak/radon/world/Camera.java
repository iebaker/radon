package xyz.izaak.radon.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.izaak.radon.exception.RenderingException;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.math.Transformable;
import xyz.izaak.radon.primitive.Primitive;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;

import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Created by ibaker on 17/08/2016.
 */
@ProvidesShaderComponents
public class Camera implements Transformable {

    public static final int NEAR_PLANE = 0;
    public static final int FAR_PLANE = 1;
    public static final int ASPECT_RATIO = 2;
    public static final int FOV = 3;

    private Vector3f eye = new Vector3f();
    private Vector3f eyePlusLook = new Vector3f();
    private Vector3f look = new Vector3f();
    private Vector3f up = new Vector3f();
    private Matrix4f view = new Matrix4f();
    private Matrix4f projection = new Matrix4f();
    private Matrix4f modifier = new Matrix4f();

    private float[] parameters = new float[4];
    private Shader shader;

    public Camera(Shader shader) {
        clearTransforms();
        this.shader = shader;
    }

    public Camera(Camera other) {
        this(other.shader);
        for(int i = 0; i < parameters.length; i++) {
            set(i, other.get(i));
        }
        this.eye = Points.copyOf(other.eye);
        this.look = Points.copyOf(other.look);
        this.eyePlusLook = Points.copyOf(other.eyePlusLook);
        this.up = Points.copyOf(other.up);
        this.view.set(other.view);
        this.projection.set(other.projection);
        this.modifier.set(other.modifier);
    }

    private void recomputeView() {
        eyePlusLook.set(eye).add(look);
        view.setLookAt(eye, eyePlusLook, up);
    }

    @Override
    public void scale(float x, float y, float z) {
        modifier.scaling(x, y, z);
        modifier.transformPosition(eye);
        recomputeView();
    }

    @Override
    public void translate(float x, float y, float z) {
        modifier.translation(x, y, z);
        modifier.transformPosition(eye);
        recomputeView();
    }

    @Override
    public void rotate(float amount, float x, float y, float z) {
        modifier.rotation(amount, x, y, z);
        modifier.transformDirection(look);
        modifier.transformDirection(up);
        recomputeView();
    }

    @Override
    public void clearTransforms() {
        eye.set(Points.ORIGIN_3D);
        up.set(Points._Y_);
        look.set(Points.__Z);
        recomputeView();
    }

    private void recomputeProjection() {
        projection.setPerspective(
                parameters[FOV],
                parameters[ASPECT_RATIO],
                parameters[NEAR_PLANE],
                parameters[FAR_PLANE]);
    }

    private void checkParameterIndex(String operation, int parameterIndex) {
        if (parameterIndex >= parameters.length) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot %s Camera parameter at index %d (max %d)",
                            operation, parameterIndex, parameters.length));
        }
    }

    public float get(int parameterIndex) {
        checkParameterIndex("get", parameterIndex);
        return parameters[parameterIndex];
    }

    public void set(int parameterIndex, float value) {
        checkParameterIndex("set", parameterIndex);
        parameters[parameterIndex] = value;
        recomputeProjection();
    }

    public void add(int parameterIndex, float value) {
        checkParameterIndex("add to", parameterIndex);
        parameters[parameterIndex] += value;
        recomputeProjection();
    }

    public void sub(int parameterIndex, float value) {
        checkParameterIndex("subtract from", parameterIndex);
        parameters[parameterIndex] -= value;
        recomputeProjection();
    }

    @ShaderUniform(identifier = Identifiers.CAMERA_EYE)
    public Vector3f getEye() {
        return eye;
    }

    public Vector3f getLook() {
        return look;
    }

    public Vector3f getUp() {
        return up;
    }

    @ShaderUniform(identifier = Identifiers.VIEW)
    public Matrix4f getView() {
        return view;
    }

    @ShaderUniform(identifier = Identifiers.PROJECTION)
    public Matrix4f getProjection() {
        return projection;
    }

    public void capture(Scene scene) throws RenderingException {
        shader.use();
        shader.setUniforms(this);

        for (Entity entity : scene.getEntities()) {
            shader.setUniforms(entity);

            for (Primitive primitive : entity.getPrimitives()) {
                primitive.bufferFor(shader);
                shader.setUniforms(primitive);

                glBindVertexArray(primitive.getVertexArrayFor(shader));
                shader.validate();
                for (Primitive.Interval interval : primitive.getIntervals()) {
                    glDrawArrays(interval.mode, interval.first, interval.count);
                }
                glBindVertexArray(0);
            }
        }
    }
}
