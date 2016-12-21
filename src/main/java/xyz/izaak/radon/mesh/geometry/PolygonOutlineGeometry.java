package xyz.izaak.radon.mesh.geometry;

import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.mesh.Mesh;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_NORMAL;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Created by ibaker on 30/11/2016.
 */
public class PolygonOutlineGeometry extends Geometry {
    private float sides;
    private int sidesI;

    public PolygonOutlineGeometry(int sides) {
        this.sides = sides;
        this.sidesI = sides;
    }

    @Override
    public void build(Mesh mesh) {
        for (float i = 0; i < sides; i++) {
            float angle = i * 2 * Points.piOver(1) / sides;
            mesh.next(VERTEX_POSITION, (float) Math.cos(angle), (float) Math.sin(angle), 0.0f);
        }
        mesh.addInterval(GL_LINE_LOOP, sidesI);
        mesh.all(VERTEX_NORMAL, Points.__Z);
    }
}
