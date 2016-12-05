package xyz.izaak.radon.primitive.geometry;

import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.primitive.Primitive;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_NORMAL;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Created by ibaker on 30/11/2016.
 */
public class QuadGeometry extends Geometry {

    @Override
    public void build(Primitive primitive) {
        Vector3f point = new Vector3f();

        primitive.next(VERTEX_POSITION, point.set(Points.xy_).mul(0.5f));
        primitive.next(VERTEX_POSITION, point.set(Points.Xy_).mul(0.5f));
        primitive.next(VERTEX_POSITION, point.set(Points.xY_).mul(0.5f));
        primitive.next(VERTEX_POSITION, point.set(Points.XY_).mul(0.5f));
        primitive.next(VERTEX_POSITION, point.set(Points.xY_).mul(0.5f));
        primitive.next(VERTEX_POSITION, point.set(Points.Xy_).mul(0.5f));
        primitive.addInterval(GL_TRIANGLES, 6);

        primitive.all(VERTEX_NORMAL, Points.__Z);
    }
}
