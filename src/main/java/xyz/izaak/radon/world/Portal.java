package xyz.izaak.radon.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.math.OrthonormalBasis;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.geometry.QuadGeometry;
import xyz.izaak.radon.mesh.geometry.QuadOutlineGeometry;
import xyz.izaak.radon.mesh.material.PortalMaterial;
import xyz.izaak.radon.mesh.material.SolidColorMaterial;

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
    private Entity outlineEntity;

    // Pre allocated workspace for crossing calculations
    private Vector4f start = new Vector4f();
    private Vector4f ray = new Vector4f();
    private Vector4f normal = new Vector4f();
    private Vector4f point = new Vector4f();
    private Matrix4f inverse = new Matrix4f();

    public Portal(Vector3f position, OrthonormalBasis frontBasis) {
        this.uuid = UUID.randomUUID();
        this.position = position;
        this.frontBasis = frontBasis;
        this.backBasis = new OrthonormalBasis(Points.copyOf(frontBasis.getI()).negate(), Points.copyOf(frontBasis.getJ()));

        Matrix4f rotation = OrthonormalBasis.rotationBetween(OrthonormalBasis.STANDARD, frontBasis);
        Mesh portalQuad = new Mesh(new QuadGeometry(), new PortalMaterial());
        portalQuad.scale(3.6f, 8.0f, 1.0f);
        this.entity = Entity.builder().build();
        this.entity.addMeshes(portalQuad);
        this.entity.transform(rotation);
        this.entity.translate(position);

        Mesh portalOutlineMesh = new Mesh(new QuadOutlineGeometry(), new SolidColorMaterial(Points.WHITE));
        portalOutlineMesh.scale(3.6f, 8.0f, 1.0f);
        portalOutlineMesh.scale(1.01f, 1.01f, 1.0f);
        this.outlineEntity = Entity.builder().build();
        this.outlineEntity.addMeshes(portalOutlineMesh);
        this.outlineEntity.transform(rotation);
        this.outlineEntity.translate(position);
    }

    public void setParentScene(Scene parentScene) {
        this.parentScene = parentScene;
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

    public Entity getOutlineEntity() {
        return outlineEntity;
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
