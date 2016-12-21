package xyz.izaak.radon.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.math.OrthonormalBasis;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.geometry.QuadGeometry;
import xyz.izaak.radon.mesh.material.PortalMaterial;

import java.util.UUID;

/**
 * Created by ibaker on 17/12/2016.
 */
public class Portal extends MatrixTransformable {
    private UUID uuid;
    private OrthonormalBasis frontBasis;
    private OrthonormalBasis backBasis;
    private Vector3f position;
    private Scene parentScene;
    private Portal childPortal;
    private Entity entity;

    // Pre allocated workspace for crossing calculations
    private Vector4f start = new Vector4f();
    private Vector4f ray = new Vector4f();
    private Vector4f normal = new Vector4f();
    private Vector4f point = new Vector4f();
    private Matrix4f inverse = new Matrix4f();

    public Portal(Scene parentScene, Vector3f position, OrthonormalBasis frontBasis) {
        this.parentScene = parentScene;
        this.position = position;
        this.frontBasis = frontBasis;
        this.backBasis = new OrthonormalBasis(Points.copyOf(frontBasis.getI()).negate(), Points.copyOf(frontBasis.getJ()));
        this.uuid = UUID.randomUUID();

        Mesh portalQuad = new Mesh(new QuadGeometry(), new PortalMaterial());
        portalQuad.setTransform(new Matrix4f(frontBasis.getMatrix()));
        portalQuad.translate(position);

        this.entity = Entity.builder().build();
        this.entity.addMeshes(portalQuad);
    }

    public OrthonormalBasis getFrontBasis() {
        return frontBasis;
    }

    public OrthonormalBasis getBackBasis() {
        return backBasis;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Portal getChildPortal() {
        return childPortal;
    }

    public Scene getParentScene() {
        return parentScene;
    }

    public Entity getEntity() {
        return entity;
    }

    public void link(Portal childPortal) {
        this.childPortal = childPortal;
    }

    public boolean crossedBy(Vector3f startPosition, Vector3f endPosition) {
        start.set(startPosition.x, startPosition.y, startPosition.z, 1.0f);
        ray.set(endPosition.x - startPosition.x, endPosition.y - startPosition.y, endPosition.z - startPosition.z, 0.0f);

        normal.set(0, 0, 1, 0);
        point.set(0, 0, 0, 1);

        inverse.set(getModel()).invert();
        inverse.transform(start);
        inverse.transform(ray);

        float term1 = normal.dot(point);
        float term2 = normal.dot(start);
        float term3 = normal.dot(ray);

        if (term3 >= 0) return false;

        float t = (term1 - term2) / term3;
        if (t >= 0 && t <= 1) {
            start.add(ray.mul(t));
            if (Math.abs(start.x) <= 1 && Math.abs(start.y) <= 1) {
                return true;
            }
        }
        return false;
    }
}
