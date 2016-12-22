package xyz.izaak.radon.world;

import org.joml.Vector3f;

/**
 * A light source which emanates from a single point in every direction.
 */
public class PointLight {
    private Vector3f intensity;
    private Vector3f position;

    /**
     * @param intensity the RGB intensity (color) of the light source
     * @param position the position of the light source in the Scene
     */
    public PointLight(Vector3f intensity, Vector3f position) {
        this.intensity = intensity;
        this.position = position;
    }

    public Vector3f getIntensity() {
        return intensity;
    }

    public Vector3f getPosition() {
        return position;
    }
}
