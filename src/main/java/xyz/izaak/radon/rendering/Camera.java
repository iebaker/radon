package xyz.izaak.radon.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.izaak.radon.exception.RenderingException;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.math.Transformable;
import xyz.izaak.radon.rendering.primitive.Primitive;
import xyz.izaak.radon.rendering.shading.Shader;
import xyz.izaak.radon.rendering.shading.ShaderComponents;
import xyz.izaak.radon.rendering.shading.ShaderVariableType;
import xyz.izaak.radon.rendering.shading.UniformStore;
import xyz.izaak.radon.world.Entity;
import xyz.izaak.radon.world.Scene;

import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Created by ibaker on 17/08/2016.
 */
public class Camera implements Transformable {

    public static final int HEIGHT_ANGLE = 1;
    public static final int NEAR_PLANE = 2;
    public static final int FAR_PLANE = 3;
    public static final int ASPECT_RATIO = 4;
    public static final int FOV = 5;

    private static final ShaderComponents shaderComponents = new ShaderComponents();
    private static final UniformStore uniformStore = new UniformStore();

    private static final String VIEW = "view";
    private static final String PROJECTION = "projection";
    private static final String CAMERA_EYE = "cameraEye";

    static {
        shaderComponents.addUniform(ShaderVariableType.MAT4, VIEW, uniformStore);
        shaderComponents.addUniform(ShaderVariableType.MAT4, PROJECTION, uniformStore);
        shaderComponents.addUniform(ShaderVariableType.VEC3, CAMERA_EYE, uniformStore);
    }

    private Vector3f eye = new Vector3f();
    private Vector3f eyePlusLook = new Vector3f();
    private Vector3f look = new Vector3f();
    private Vector3f up = new Vector3f();
    private Matrix4f view = new Matrix4f();
    private Matrix4f projection = new Matrix4f();
    private Matrix4f modifier = new Matrix4f();

    private float[] parameters = new float[5];
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

    private void updateUniformStore() {
        uniformStore.updateUniformMatrix4f(VIEW).set(view);
        uniformStore.updateUniformMatrix4f(PROJECTION).set(projection);
        uniformStore.updateUniformVector3f(CAMERA_EYE).set(eye);
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

    public Vector3f getEye() {
        return eye;
    }

    public Vector3f getLook() {
        return look;
    }

    public Vector3f getUp() {
        return up;
    }

    public Matrix4f getView() {
        return view;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public void capture(Scene scene) throws RenderingException {
        this.updateUniformStore();
        for (Entity entity : scene.getEntities()) {
            for (Primitive primitive : entity.getPrimitives()) {
                primitive.updateUniformStore(entity.getModel());
                primitive.bufferFor(shader);
                shader.setUniforms();
                shader.validate();
                shader.use();

                glBindVertexArray(primitive.getVertexArrayFor(shader));
                for (Primitive.Interval interval : primitive.getIntervals()) {
                    glDrawArrays(interval.mode, interval.first, interval.count);
                }
                glBindVertexArray(0);
            }
        }
    }
}
