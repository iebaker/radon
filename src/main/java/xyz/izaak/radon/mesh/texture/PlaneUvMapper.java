package xyz.izaak.radon.mesh.texture;


import org.joml.Vector3f;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.MeshBuilder;

import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_UV;

/**
 * Created by ibaker on 27/12/2016.
 */
public class PlaneUvMapper implements MeshBuilder {

    @Override
    public void build(Mesh mesh) {
        float[] outputBuffer = new float[2];
        mesh.derive(VERTEX_UV, VERTEX_POSITION, (Vector3f vertexPosition) -> {
            outputBuffer[0] = (int) (0.5f * Math.signum(vertexPosition.x) + 0.5f);
            outputBuffer[1] = (int) (0.5f * Math.signum(vertexPosition.y) + 0.5f);
            return outputBuffer;
        });
    }
}
