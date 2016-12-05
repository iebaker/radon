package xyz.izaak.radon.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.izaak.radon.exception.RadonException;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.math.Transformable;
import xyz.izaak.radon.primitive.Primitive;
import xyz.izaak.radon.primitive.geometry.Geometry;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;
import xyz.izaak.radon.shading.annotation.VertexShaderMain;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * Created by ibaker on 17/08/2016.
 */
@ProvidesShaderComponents(requires = {Geometry.class, Primitive.class, Entity.class})
public class Camera implements Transformable {

    public static final int NEAR_PLANE = 0;
    public static final int FAR_PLANE = 1;
    public static final int ASPECT_RATIO = 2;
    public static final int FOV = 3;

    private static Set<Shader> shaders = new HashSet<>();

    private Vector3f eye = new Vector3f();
    private Vector3f eyePlusLook = new Vector3f();
    private Vector3f look = new Vector3f();
    private Vector3f up = new Vector3f();
    private Matrix4f view = new Matrix4f();
    private Matrix4f projection = new Matrix4f();
    private Matrix4f modifier = new Matrix4f();

    private float[] parameters = new float[4];
    private Shader shader;

    public static void registerShader(Shader shader) {
        shaders.add(shader);
    }

    public static void compileAndRegisterShader(ShaderCompiler shaderCompiler) {
        try {
            Shader shader = shaderCompiler.compile("" + System.nanoTime());
            shaders.add(shader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float nearPlane = 0.1f;
        private float farPlane = 1000.0f;
        private float aspectRatio = 1.3f;
        private float fov = Points.piOver(2);
        private Vector3f eye = Points.copyOf(Points.__z);
        private Vector3f look = Points.copyOf(Points.__Z);
        private Vector3f up = Points.copyOf(Points._Y_);

        public Builder nearPlane(float nearPlane) {
            this.nearPlane = nearPlane;
            return this;
        }

        public Builder farPlane(float farPlane) {
            this.farPlane = farPlane;
            return this;
        }

        public Builder aspectRatio(float aspectRatio) {
            this.aspectRatio = aspectRatio;
            return this;
        }

        public Builder fov(float fov) {
            this.fov = fov;
            return this;
        }

        public Builder eye(Vector3f eye) {
            this.eye.set(eye);
            return this;
        }

        public Builder look(Vector3f look) {
            this.look.set(look);
            return this;
        }

        public Builder up(Vector3f up) {
            this.up.set(up);
            return this;
        }

        public Camera build() {
            return new Camera(nearPlane, farPlane, aspectRatio, fov, eye, look, up);
        }
    }

    public Camera(
            float nearPlane,
            float farPlane,
            float aspectRatio,
            float fov,
            Vector3f eye,
            Vector3f look,
            Vector3f up) {
        this.eye.set(eye);
        this.up.set(up);
        this.look.set(look);
        recomputeView();
        this.set(NEAR_PLANE, nearPlane);
        this.set(FAR_PLANE, farPlane);
        this.set(ASPECT_RATIO, aspectRatio);
        this.set(FOV, fov);
    }

    public Camera(Camera other) {
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

    @Override
    public void setTransform(Matrix4f transform) {
        clearTransforms();
        transform.transformPosition(eye);
        transform.transformDirection(up);
        transform.transformDirection(look);
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

    @VertexShaderMain
    public static String setGlPosition() {
        return "gl_Position = rn_Projection * rn_View * rn_EntityModel" +
                " * rn_PrimitiveModel * vec4(rn_VertexPosition, 1);\n";
    }

    public Shader selectShaderFor(Primitive primitive) throws RadonException {
        Shader selected = null;
        for (Shader shader : shaders) {
            if (shader.supports(primitive.getMaterial().getClass())) {
                selected = shader;
                break;
            }
        }
        if (selected != null) {
            return selected;
        }
        throw new RadonException(
                String.format("Could not find shader which supports primitive %s with material %s",
                        primitive.toString(), primitive.getMaterial().toString()));
    }

    public void capture(Scene scene) throws RadonException {
        for (Entity entity : scene.getEntities()) {
            for (Primitive primitive : entity.getPrimitives()) {
                Shader shader = selectShaderFor(primitive);
                shader.use();
                shader.setUniforms(this);
                shader.setUniforms(scene);
                shader.setUniforms(entity);
                shader.setUniforms(primitive);
                shader.setUniforms(primitive.getGeometry());
                shader.setUniforms(primitive.getMaterial());
                primitive.bufferFor(shader);

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
