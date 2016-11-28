package xyz.izaak.radon.primitive;

import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_COLOR;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;

/**
 * Created by ibaker on 28/11/2016.
 */
public class PolarSpherePrimitive extends Primitive implements FilledStroked {
    private int longitudeLines;
    private int latitudeLines;

    public PolarSpherePrimitive(int longitudeLines, int latitudeLines) {
        this.longitudeLines = longitudeLines;
        this.latitudeLines = latitudeLines;
    }

    @Override
    public void build() {
        float azimuthAngle;
        float zenithAngle = Points.piOver(1) / (float) (latitudeLines + 1);

        // top cap
        next(VERTEX_POSITION, Points.copyOf(Points.__Z));
        for (float i = 0; i <= longitudeLines; i++) {
            azimuthAngle = i * 2 * Points.piOver(1) / (float) longitudeLines;
            next(VERTEX_POSITION, toCartesianCoordinates(azimuthAngle, zenithAngle));
        }
        addInterval(GL_TRIANGLE_FAN, longitudeLines + 2);

        // strips around the body
        float zenithAngleTop = zenithAngle;
        for (float i = 1; i < latitudeLines; i++) {
            zenithAngle = i * Points.piOver(1) / (float) (latitudeLines + 1);
            zenithAngleTop = (i + 1) * Points.piOver(1) / (float) (latitudeLines + 1);
            for (float j = 0; j <= longitudeLines; j++) {
                azimuthAngle = j * 2 * Points.piOver(1) / (float) longitudeLines;
                next(VERTEX_POSITION, toCartesianCoordinates(azimuthAngle, zenithAngle));
                next(VERTEX_POSITION, toCartesianCoordinates(azimuthAngle, zenithAngleTop));
            }
            addInterval(GL_TRIANGLE_STRIP, 2 * (longitudeLines + 1));
        }

        // bottom cap
        next(VERTEX_POSITION, Points.copyOf(Points.__z));
        for (float i = longitudeLines; i >= 0; i--) {
            azimuthAngle = i * 2 * Points.piOver(1) / (float) longitudeLines;
            next(VERTEX_POSITION, toCartesianCoordinates(azimuthAngle, zenithAngleTop));
        }
        addInterval(GL_TRIANGLE_FAN, longitudeLines + 2);

        // lines of latitude
        for (int i = 1; i < latitudeLines + 1; i++) {
            zenithAngle = i * Points.piOver(1) / (float) (latitudeLines + 1);
            for (float j = 0; j < longitudeLines; j++) {
                azimuthAngle = j * 2 * Points.piOver(1) / (float) longitudeLines;
                next(VERTEX_POSITION, toCartesianCoordinates(azimuthAngle, zenithAngle));
            }
            addInterval(GL_LINE_LOOP, longitudeLines);
        }

        // lines of longitude
        for (int i = 0; i < longitudeLines; i++) {
            azimuthAngle = i * 2 * Points.piOver(1) / (float) longitudeLines;
            for (float j = 0; j < 2 * (latitudeLines + 1); j++) {
                zenithAngle = j * 2 * Points.piOver(1) / (float) (2 * (latitudeLines + 2));
                next(VERTEX_POSITION, toCartesianCoordinates(azimuthAngle, zenithAngle));
            }
            addInterval(GL_LINE_LOOP, 2 * (latitudeLines + 1));
        }

    }

    private Vector3f toCartesianCoordinates(float azimuthAngle, float zenithAngle) {
        float x = (float) (Math.sin(zenithAngle) * Math.cos(azimuthAngle));
        float y = (float) (Math.sin(zenithAngle) * Math.sin(azimuthAngle));
        float z = (float) (Math.cos(zenithAngle));
        return new Vector3f(x, y, z);
    }

    @Override
    public void setFillColor(Vector3f color) {
        int fillStart = 0;
        int fillEnd = (2 * (longitudeLines + 2)) + ((latitudeLines - 1) * (2 * (longitudeLines + 2)));
        range(fillStart, fillEnd, VERTEX_COLOR, color);
    }

    @Override
    public void setStrokeColor(Vector3f color) {
        int strokeStart = (2 * (longitudeLines + 2)) + ((latitudeLines - 1) * (2 * (longitudeLines + 2)));
        int strokeEnd = strokeStart + ((latitudeLines - 1) * longitudeLines) + (longitudeLines * (2 * (latitudeLines + 1)));
        range(strokeStart, strokeEnd, VERTEX_COLOR, color);
    }
}
