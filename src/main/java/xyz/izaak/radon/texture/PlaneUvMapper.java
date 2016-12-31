package xyz.izaak.radon.texture;


import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.MeshBuilder;

import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_UV;

/**
 * Created by ibaker on 27/12/2016.
 */
public class PlaneUvMapper implements MeshBuilder {

    private Vector2f uvMin = new Vector2f();
    private Vector2f uvMax = new Vector2f();

    public PlaneUvMapper(float uMin, float vMin, float uMax, float vMax) {
        this.uvMin.set(uMin, vMin);
        this.uvMax.set(uMax, vMax);
    }

    public PlaneUvMapper() {
        this(0, 0, 1, 1);
    }

    @Override
    public void build(Mesh mesh) {
        float[] outputBuffer = new float[2];
        mesh.derive(VERTEX_UV, VERTEX_POSITION, (Vector3f vertexPosition) -> {
            outputBuffer[0] = vertexPosition.x > 0 ? uvMax.x : uvMin.x;
            outputBuffer[1] = vertexPosition.y > 0 ? uvMax.y : uvMin.y;
            return outputBuffer;
        });
    }
}
