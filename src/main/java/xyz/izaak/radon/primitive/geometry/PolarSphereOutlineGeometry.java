package xyz.izaak.radon.primitive.geometry;

import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.primitive.Primitive;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_COLOR;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_NORMAL;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Created by ibaker on 30/11/2016.
 */
public class PolarSphereOutlineGeometry extends Geometry {

    private int longitudeLines;
    private int latitudeLines;

    public PolarSphereOutlineGeometry(int longitudeLines, int latitudeLines) {
        this.longitudeLines = longitudeLines;
        this.latitudeLines = latitudeLines;
    }

    @Override
    public void build(Primitive primitive) {
        float zenithAngle, azimuthAngle;
        Vector3f vector = new Vector3f();

        // lines of latitude
        for (int i = 1; i < latitudeLines + 1; i++) {
            zenithAngle = i * Points.piOver(1) / (float) (latitudeLines + 1);
            for (float j = 0; j < longitudeLines; j++) {
                azimuthAngle = j * 2 * Points.piOver(1) / (float) longitudeLines;

                Points.setToCartesianCoordinates(vector, azimuthAngle, zenithAngle);
                primitive.next(VERTEX_POSITION, vector);
                primitive.next(VERTEX_COLOR, vector);
            }
            primitive.addInterval(GL_LINE_LOOP, longitudeLines);
        }

        // lines of longitude
        for (int i = 0; i < longitudeLines; i++) {
            azimuthAngle = i * 2 * Points.piOver(1) / (float) longitudeLines;
            for (float j = 0; j < 2 * (latitudeLines + 1); j++) {
                zenithAngle = j * 2 * Points.piOver(1) / (float) (2 * (latitudeLines + 2));

                Points.setToCartesianCoordinates(vector, azimuthAngle, zenithAngle);
                primitive.next(VERTEX_POSITION, vector);
                primitive.next(VERTEX_NORMAL, vector);
            }
            primitive.addInterval(GL_LINE_LOOP, 2 * (latitudeLines + 1));
        }
    }
}
