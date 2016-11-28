package xyz.izaak.radon.primitive;

import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_COLOR;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Created by ibaker on 27/08/2016.
 */
public class PolygonPrimitive extends Primitive implements FilledStroked {
    private float sides;
    private int sidesI;

    public PolygonPrimitive(int sides) {
        this.sides = sides;
        this.sidesI = sides;
    }

    @Override
    public void build() {
        next(VERTEX_POSITION, Points.copyOf(Points.ORIGIN_3D));
        for (float i = 0; i <= sides; i++) {
            float angle = i * 2 * Points.piOver(1) / sides;
            next(VERTEX_POSITION, (float) Math.cos(angle), (float) Math.sin(angle), 0.0f);
        }
        addInterval(GL_TRIANGLE_FAN, sidesI + 2);
        for (float i = 0; i < sides; i++) {
            float angle = i * 2 * Points.piOver(1) / sides;
            next(VERTEX_POSITION, (float) Math.cos(angle), (float) Math.sin(angle), 0.0f);
        }
        addInterval(GL_LINE_LOOP, sidesI);
    }

    @Override
    public void setFillColor(Vector3f color) {
        range(0, sidesI + 2, VERTEX_COLOR, color);
    }

    @Override
    public void setStrokeColor(Vector3f color) {
        range(sidesI + 2, sidesI + sidesI + 2, VERTEX_COLOR, color);
    }
}
