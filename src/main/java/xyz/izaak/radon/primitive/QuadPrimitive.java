package xyz.izaak.radon.primitive;

import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_COLOR;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Created by ibaker on 27/08/2016.
 */
public class QuadPrimitive extends Primitive implements FilledStroked {
    @Override
    public void build() {
        Vector3f point = new Vector3f();

        next(VERTEX_POSITION, point.set(Points.xy_).mul(0.5f));
        next(VERTEX_POSITION, point.set(Points.Xy_).mul(0.5f));
        next(VERTEX_POSITION, point.set(Points.xY_).mul(0.5f));
        next(VERTEX_POSITION, point.set(Points.XY_).mul(0.5f));
        next(VERTEX_POSITION, point.set(Points.xY_).mul(0.5f));
        next(VERTEX_POSITION, point.set(Points.Xy_).mul(0.5f));
        addInterval(GL_TRIANGLES, 6);

        next(VERTEX_POSITION, point.set(Points.xy_).mul(0.5f));
        next(VERTEX_POSITION, point.set(Points.Xy_).mul(0.5f));
        next(VERTEX_POSITION, point.set(Points.XY_).mul(0.5f));
        next(VERTEX_POSITION, point.set(Points.xY_).mul(0.5f));
        addInterval(GL_LINE_LOOP, 4);
    }

    @Override
    public void setFillColor(Vector3f color) {
        range(0, 6, VERTEX_COLOR, color);
    }

    @Override
    public void setStrokeColor(Vector3f color) {
        range(6, 4, VERTEX_COLOR, color);
    }
}
