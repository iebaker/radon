package xyz.izaak.radon.world;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.izaak.radon.math.Basis;
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
    public static Vector2f PORTAL_DIMENSIONS = new Vector2f(3.6f, 8.0f);

    private UUID uuid;
    private OrthonormalBasis frontBasis;
    private OrthonormalBasis backBasis;
    private Vector3f position;
    private Scene parentScene;
    private Portal childPortal;
    private Entity entity;
    private Entity outlineEntity;
    private Vector4f plane;

    private Vector3f scratch = new Vector3f();
    private Vector3f projection = new Vector3f();

    public Portal(Vector3f position, OrthonormalBasis frontBasis) {
        this.uuid = UUID.randomUUID();
        this.position = position;
        this.frontBasis = frontBasis;
        this.backBasis = new OrthonormalBasis(Points.copyOf(frontBasis.getI()).negate(), Points.copyOf(frontBasis.getJ()));

        Matrix4f rotation = OrthonormalBasis.rotationBetween(OrthonormalBasis.STANDARD, frontBasis);
        Mesh portalQuad = new Mesh(new QuadGeometry(), new PortalMaterial());
        portalQuad.scale(PORTAL_DIMENSIONS.x, PORTAL_DIMENSIONS.y, 1.0f);
        this.entity = Entity.builder().build();
        this.entity.addMeshes(portalQuad);
        this.entity.transform(rotation);
        this.entity.translate(position);

        Mesh portalOutlineMesh = new Mesh(new QuadOutlineGeometry(), new SolidColorMaterial(Points.WHITE));
        portalOutlineMesh.scale(PORTAL_DIMENSIONS.x, PORTAL_DIMENSIONS.y, 1.0f);
        portalOutlineMesh.scale(1.01f, 1.01f, 1.0f);
        this.outlineEntity = Entity.builder().build();
        this.outlineEntity.addMeshes(portalOutlineMesh);
        this.outlineEntity.transform(rotation);
        this.outlineEntity.translate(position);

        Vector3f normal = this.frontBasis.getK();
        this.plane = new Vector4f(normal.x, normal.y, normal.z, -normal.dot(this.position));
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

    public Vector4f getPlane() {
        return plane;
    }

    public void link(Portal childPortal) {
        this.childPortal = childPortal;
    }

    public void transformPosition(Vector3f position) {
        if (childPortal == null) return;
        scratch.set(position).sub(getPosition());
        Basis.change(scratch, OrthonormalBasis.STANDARD, frontBasis);
        Basis.change(scratch, childPortal.getBackBasis(), OrthonormalBasis.STANDARD);
        position.set(childPortal.getPosition()).add(scratch);
    }

    public void transformDirection(Vector3f direction) {
        Basis.change(direction, OrthonormalBasis.STANDARD, frontBasis);
        Basis.change(direction, childPortal.getBackBasis(), OrthonormalBasis.STANDARD);
    }

    public boolean crossedBy(Vector3f source, Vector3f direction) {
        float directionDotNormal = direction.dot(frontBasis.getK());
        if (directionDotNormal >= 0) return false;

        // t is the distance to the plane in units of the length of direction
        float t = scratch.set(position).sub(source).dot(frontBasis.getK()) / directionDotNormal;
        if (t < 0 || t > 1) return false;

        // scratch now points from the center of the portal to the point the
        // ray intersects the portal's plane
        scratch.set(direction).mul(t).add(source).sub(position);

        // projection is the horizontal component relative to the portal
        Points.project(scratch, frontBasis.getI(), projection);
        boolean hitsHorizontally = projection.lengthSquared() <= PORTAL_DIMENSIONS.x * PORTAL_DIMENSIONS.x / 4.0f;

        // projection is the vertical component relative to the portal
        Points.project(scratch, frontBasis.getJ(), projection);
        boolean hitsVertically = projection.lengthSquared() <= PORTAL_DIMENSIONS.y * PORTAL_DIMENSIONS.y / 4.0f;

        return hitsHorizontally && hitsVertically;
    }
}
