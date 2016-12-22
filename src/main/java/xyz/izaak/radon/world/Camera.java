package xyz.izaak.radon.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.izaak.radon.exception.RadonException;
import xyz.izaak.radon.math.Basis;
import xyz.izaak.radon.math.OrthonormalBasis;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.math.Transformable;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.geometry.Geometry;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;
import xyz.izaak.radon.shading.UniformProvider;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;
import xyz.izaak.radon.shading.annotation.VertexShaderMain;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_DECR;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_INCR;
import static org.lwjgl.opengl.GL11.GL_KEEP;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glStencilOp;
import static org.lwjgl.opengl.GL30.glBindVertexArray;


@ProvidesShaderComponents(requires = {Geometry.class, Mesh.class, Entity.class})
public class Camera implements Transformable, UniformProvider {


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
    private Vector3f scratch = new Vector3f();

    private float[] parameters = new float[4];
    private int maxPortalDepth;
    private Shader shader;

    public static void registerShader(Shader shader) {
        shaders.add(shader);
    }

    @VertexShaderMain
    public static String setGlPosition() {
        return "gl_Position = rn_Projection * rn_View * rn_EntityModel * rn_MeshModel * vec4(rn_VertexPosition, 1);\n";
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
        private int maxPortalDepth = 5;
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

        public Builder maxPortalDepth(int maxPortalDepth) {
            this.maxPortalDepth = maxPortalDepth;
            return this;
        }

        public Camera build() {
            return new Camera(nearPlane, farPlane, aspectRatio, fov, eye, look, up, maxPortalDepth);
        }
    }

    public Camera(
            float nearPlane,
            float farPlane,
            float aspectRatio,
            float fov,
            Vector3f eye,
            Vector3f look,
            Vector3f up,
            int maxPortalDepth) {
        this.eye.set(eye);
        this.up.set(up);
        this.look.set(look);
        recomputeView();
        this.set(NEAR_PLANE, nearPlane);
        this.set(FAR_PLANE, farPlane);
        this.set(ASPECT_RATIO, aspectRatio);
        this.set(FOV, fov);
        recomputeProjection();
        this.maxPortalDepth = maxPortalDepth;
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

    @ShaderUniform(identifier = Identifiers.CAMERA_EYE)
    public Vector3f getEye() {
        return eye;
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.VIEW, getView());
        shader.setUniform(Identifiers.PROJECTION, getProjection());
        shader.setUniform(Identifiers.CAMERA_EYE, getEye());
    }

    /**
     * Performs a forward rendering pipeline to render a Scene.
     * Invokes OpenGL's drawArrays method for each component of each Primitive of each Entity in a Scene, rendering
     * it to the currently bound Framebuffer (for example, the application window). Each Primitive will be rendered
     * with an appropriate shader for its Material if one has been {@link Camera#registerShader(Shader) registered}.
     *
     * @param scene the scene to render
     * @throws RadonException if anything detectable goes wrong during rendering
     */
    public void capture(Scene scene) throws RadonException {
        glEnable(GL_STENCIL_TEST);
        capture(scene, 0, null);
        glDisable(GL_STENCIL_TEST);
    }

    /* ============================================= *
     * Implementation of the Transformable interface *
     * ============================================= */

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
    public void transform(Matrix4f transform) {
        transform.transformPosition(eye);
        transform.transformDirection(up);
        transform.transformDirection(look);
        recomputeView();
    }

    private void capture(Scene scene, int depth, Portal throughPortal) throws RadonException {
        if (scene == null) return;

        if (depth < maxPortalDepth) {
            for (Portal portal : scene.getPortals()) {
                if (portal == throughPortal || portal.getChildPortal() == null) continue;
                stencilPortal(portal, depth);
                shiftPerspective(portal, portal.getChildPortal());
                capture(portal.getChildPortal().getParentScene(), depth + 1, portal.getChildPortal());
                shiftPerspective(portal.getChildPortal(), portal);
                protectPortal(portal, depth);
            }
        }

        glColorMask(true, true, true, true);
        glDepthMask(true);
        glStencilFunc(GL_EQUAL, depth, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        for (Entity entity : scene.getEntities()) {
            for (Mesh mesh : entity.getMeshes()) {
                renderMesh(mesh, this, scene, entity, mesh, mesh.getGeometry(), mesh.getMaterial());
            }
        }

        for (Portal portal : scene.getPortals()) {
            Entity outline = portal.getOutlineEntity();
            for (Mesh mesh : outline.getMeshes()) {
                renderMesh(mesh, this, scene, outline, mesh, mesh.getGeometry(), mesh.getMaterial());
            }
        }
    }

    private void stencilPortal(Portal portal, int depth) throws RadonException {
        glColorMask(false, false, false, false);
        glDepthMask(false);
        glStencilFunc(GL_EQUAL, depth, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_INCR);

        Entity portalEntity = portal.getEntity();
        Mesh portalMesh = portalEntity.getMeshes().iterator().next();
        renderMesh(portalMesh, this, portalEntity, portalMesh, portalMesh.getGeometry(), portalMesh.getMaterial());
    }

    private void protectPortal(Portal portal, int depth) throws RadonException {
        glColorMask(false, false, false, false);
        glDepthMask(true);
        glStencilFunc(GL_EQUAL, depth + 1, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_DECR);

        Entity portalEntity = portal.getEntity();
        Mesh portalMesh = portalEntity.getMeshes().iterator().next();
        renderMesh(portalMesh, this, portalEntity, portalMesh, portalMesh.getGeometry(), portalMesh.getMaterial());
    }

    private void renderMesh(Mesh mesh, UniformProvider... uniformProviders) throws RadonException{
        Shader shader = selectShaderFor(mesh);
        shader.use();
        for (UniformProvider uniformProvider : uniformProviders) {
            uniformProvider.setUniformsOn(shader);
        }
        mesh.bufferFor(shader);
        glBindVertexArray(mesh.getVertexArrayFor(shader));
        shader.validate();
        for (Mesh.Interval interval : mesh.getIntervals()) {
            glDrawArrays(interval.mode, interval.first, interval.count);
        }
        glBindVertexArray(0);
    }

    private void shiftPerspective(Portal local, Portal remote) {
        scratch.set(eye).sub(local.getPosition());
        Basis.change(scratch, OrthonormalBasis.STANDARD, local.getFrontBasis());
        Basis.change(scratch, remote.getBackBasis(), OrthonormalBasis.STANDARD);
        eye.set(remote.getPosition()).add(scratch);

        Basis.change(look, OrthonormalBasis.STANDARD, local.getFrontBasis());
        Basis.change(look, remote.getBackBasis(), OrthonormalBasis.STANDARD);

        Basis.change(up, OrthonormalBasis.STANDARD, local.getFrontBasis());
        Basis.change(up, remote.getBackBasis(), OrthonormalBasis.STANDARD);

        recomputeView();
    }

    private void recomputeView() {
        eyePlusLook.set(eye).add(look);
        view.setLookAt(eye, eyePlusLook, up);
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

    private Shader selectShaderFor(Mesh mesh) throws RadonException {
        Shader selected = null;
        for (Shader shader : shaders) {
            if (shader.supports(mesh.getMaterial().getClass())) {
                selected = shader;
                break;
            }
        }
        if (selected != null) {
            return selected;
        }
        throw new RadonException(
                String.format("Could not find shader which supports mesh %s with material %s",
                        mesh.toString(), mesh.getMaterial().toString()));
    }
}
