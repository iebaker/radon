package xyz.izaak.radon.geometry;

import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.mesh.Mesh;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_NORMAL;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Created by ibaker on 30/11/2016.
 */
public class PolygonGeometry extends Geometry {
    private float sides;
    private int sidesI;

    public PolygonGeometry(int sides) {
        this.sides = sides;
        this.sidesI = sides;
    }

    @Override
    public void build(Mesh mesh) {
        mesh.next(VERTEX_POSITION, Points.ORIGIN_3D);
        for (float i = 0; i <= sides; i++) {
            float angle = i * 2 * Points.piOver(1) / sides;
            mesh.next(VERTEX_POSITION, (float) Math.cos(angle), (float) Math.sin(angle), 0.0f);
        }
        mesh.addInterval(GL_TRIANGLE_FAN, sidesI + 2);
        mesh.all(VERTEX_NORMAL, Points.__Z);
    }
}
