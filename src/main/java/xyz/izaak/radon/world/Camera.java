package xyz.izaak.radon.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.izaak.radon.exception.RadonException;
import xyz.izaak.radon.math.OrthonormalBasis;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.math.Transformable;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.UniformProvider;
import java.util.List;

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

public class Camera implements Transformable, UniformProvider {

    public static final int NEAR_PLANE = 0;
    public static final int FAR_PLANE = 1;
    public static final int ASPECT_RATIO = 2;
    public static final int FOV = 3;

    private OrthonormalBasis defaultOrientation;
    private Vector3f eye = new Vector3f();
    private Vector3f eyePlusLook = new Vector3f();
    private Vector3f look = new Vector3f();
    private Vector3f up = new Vector3f();
    private Matrix4f view = new Matrix4f();
    private Matrix4f projection = new Matrix4f();
    private Matrix4f modifier = new Matrix4f();
    private Vector4f fragmentDiscardPlane = new Vector4f();
    private float[] parameters = new float[4];
    private int maxPortalDepth;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxPortalDepth = 5;
        private float nearPlane = 0.1f;
        private float farPlane = 1000.0f;
        private float aspectRatio = 1.3f;
        private float fov = Points.piOver(2);
        private Vector3f eye = Points.copyOf(Points.ORIGIN_3D);
        private Vector3f look = Points.copyOf(Points._Y_);
        private Vector3f up = Points.copyOf(Points.__Z);
        private OrthonormalBasis defaultOrientation = new OrthonormalBasis(look, up);

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
            return new Camera(defaultOrientation, nearPlane, farPlane, aspectRatio, fov, eye, look, up, maxPortalDepth);
        }
    }

    public Camera(
            OrthonormalBasis defaultOrientation,
            float nearPlane,
            float farPlane,
            float aspectRatio,
            float fov,
            Vector3f eye,
            Vector3f look,
            Vector3f up,
            int maxPortalDepth) {

        this.defaultOrientation = defaultOrientation;

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

    public Vector3f getEye() {
        return eye;
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.VIEW, view);
        shader.setUniform(Identifiers.PROJECTION, projection);
        shader.setUniform(Identifiers.CAMERA_EYE, eye);
        if (fragmentDiscardPlane != null) {
            shader.setUniform(Identifiers.USE_DISCARD_PLANE, true);
            shader.setUniform(Identifiers.DISCARD_PLANE, fragmentDiscardPlane);
        } else {
            shader.setUniform(Identifiers.USE_DISCARD_PLANE, false);
        }
    }

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
        look.set(defaultOrientation.getI());
        up.set(defaultOrientation.getJ());
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

        List<Portal> portals = scene.getPortals();
        int portalCount = portals.size();

        if (depth < maxPortalDepth) {
            for (int i = 0; i < portalCount; i++) {
                Portal portal = portals.get(i);
                if (portal == throughPortal || portal.getChildPortal() == null) continue;
                stencilPortal(portal, depth);
                shiftPerspective(portal);
                capture(portal.getChildPortal().getParentScene(), depth + 1, portal.getChildPortal());
                shiftPerspective(portal.getChildPortal());
                protectPortal(portal, depth);
            }
        }

        glColorMask(true, true, true, true);
        glDepthMask(true);
        glStencilFunc(GL_EQUAL, depth, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        if (throughPortal != null) {
            fragmentDiscardPlane = throughPortal.getPlane();
        } else {
            fragmentDiscardPlane = null;
        }

        List<Entity> entities = scene.getEntities();
        int entityCount = entities.size();
        for (int i = 0; i < entityCount; i++) {
            Entity entity = entities.get(i);
            List<Mesh> meshes = entity.getMeshes();
            int meshCount = meshes.size();
            for (int j = 0; j < meshCount; j++) {
                Mesh mesh = meshes.get(j);
                renderMesh(mesh, this, scene, entity, mesh, mesh.getGeometry(), mesh.getMaterial());
            }
        }

        for (int i = 0; i < portalCount; i++) {
            Entity outline = portals.get(i).getOutlineEntity();
            List<Mesh> meshes = outline.getMeshes();
            int meshCount = meshes.size();
            for (int j = 0; j < meshCount; j++) {
                Mesh mesh = meshes.get(j);
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
        Mesh portalMesh = portalEntity.getMeshes().get(0);
        renderMesh(portalMesh, this, portalEntity, portalMesh, portalMesh.getGeometry(), portalMesh.getMaterial());
    }

    private void protectPortal(Portal portal, int depth) throws RadonException {
        glColorMask(false, false, false, false);
        glDepthMask(true);
        glStencilFunc(GL_EQUAL, depth + 1, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_DECR);

        Entity portalEntity = portal.getEntity();
        Mesh portalMesh = portalEntity.getMeshes().get(0);
        renderMesh(portalMesh, this, portalEntity, portalMesh, portalMesh.getGeometry(), portalMesh.getMaterial());
    }

    private void renderMesh(Mesh mesh, UniformProvider... uniformProviders) throws RadonException{
        Shader shader = mesh.getMaterial().getShader();
        shader.use();

        int uniformProviderCount = uniformProviders.length;
        for (int i = 0; i < uniformProviderCount; i++) {
            uniformProviders[i].setUniformsOn(shader);
        }

        mesh.bufferFor(shader);
        glBindVertexArray(mesh.getVertexArrayFor(shader));
        shader.validate();

        int numIntervals = mesh.getIntervals().size();
        for (int i = 0; i < numIntervals; i++) {
            Mesh.Interval interval = mesh.getIntervals().get(i);
            glDrawArrays(interval.mode, interval.first, interval.count);
        }

        glBindVertexArray(0);
    }

    public void shiftPerspective(Portal portal) {
        portal.transformPosition(eye);
        portal.transformDirection(look);
        portal.transformDirection(up);
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
}
