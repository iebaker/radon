package xyz.izaak.radon.geometry;

import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.mesh.Mesh;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_NORMAL;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Created by ibaker on 21/12/2016.
 */
public class QuadOutlineGeometry extends Geometry {

    @Override
    public void build(Mesh mesh) {
        Vector3f point = new Vector3f();

        mesh.next(VERTEX_POSITION, point.set(Points.xy_).mul(0.5f));
        mesh.next(VERTEX_POSITION, point.set(Points.xY_).mul(0.5f));
        mesh.next(VERTEX_POSITION, point.set(Points.XY_).mul(0.5f));
        mesh.next(VERTEX_POSITION, point.set(Points.Xy_).mul(0.5f));
        mesh.addInterval(GL_LINE_LOOP, 4);

        mesh.all(VERTEX_NORMAL, point.set(Points.__Z));
    }
}
