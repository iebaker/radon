package xyz.izaak.radon.primitive.geometry;

import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.primitive.Primitive;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_NORMAL;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

public class PolarSphereGeometry extends Geometry {
    private int longitudeLines;
    private int latitudeLines;

    public PolarSphereGeometry(int longitudeLines, int latitudeLines) {
        this.longitudeLines = longitudeLines;
        this.latitudeLines = latitudeLines;
    }

    @Override
    public void build(Primitive primitive) {
        float azimuthAngle;
        float zenithAngle = Points.piOver(1) / (float) (latitudeLines + 1);
        Vector3f vector = new Vector3f();

        // top cap
        primitive.next(VERTEX_POSITION, Points.__Z);
        primitive.next(VERTEX_NORMAL, Points.__Z);
        for (float i = 0; i <= longitudeLines; i++) {
            azimuthAngle = i * 2 * Points.piOver(1) / (float) longitudeLines;

            Points.setToCartesianCoordinates(vector, azimuthAngle, zenithAngle);
            primitive.next(VERTEX_POSITION, vector);
            primitive.next(VERTEX_NORMAL, vector);
        }
        primitive.addInterval(GL_TRIANGLE_FAN, longitudeLines + 2);

        // strips around the body
        float zenithAngleTop = zenithAngle;
        for (float i = 1; i < latitudeLines; i++) {
            zenithAngle = i * Points.piOver(1) / (float) (latitudeLines + 1);
            zenithAngleTop = (i + 1) * Points.piOver(1) / (float) (latitudeLines + 1);
            for (float j = 0; j <= longitudeLines; j++) {
                azimuthAngle = j * 2 * Points.piOver(1) / (float) longitudeLines;

                Points.setToCartesianCoordinates(vector, azimuthAngle, zenithAngle);
                primitive.next(VERTEX_POSITION, vector);
                primitive.next(VERTEX_NORMAL, vector);

                Points.setToCartesianCoordinates(vector, azimuthAngle, zenithAngleTop);
                primitive.next(VERTEX_POSITION, vector);
                primitive.next(VERTEX_NORMAL, vector);
            }
            primitive.addInterval(GL_TRIANGLE_STRIP, 2 * (longitudeLines + 1));
        }

        // bottom cap
        primitive.next(VERTEX_POSITION, Points.copyOf(Points.__z));
        for (float i = longitudeLines; i >= 0; i--) {
            azimuthAngle = i * 2 * Points.piOver(1) / (float) longitudeLines;

            Points.setToCartesianCoordinates(vector, azimuthAngle, zenithAngle);
            primitive.next(VERTEX_POSITION, vector);
            primitive.next(VERTEX_NORMAL, vector);
        }
        primitive.addInterval(GL_TRIANGLE_FAN, longitudeLines + 2);
    }
}
