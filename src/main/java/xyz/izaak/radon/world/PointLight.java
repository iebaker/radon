package xyz.izaak.radon.world;

import org.joml.Vector3f;

/**
 * Created by ibaker on 03/12/2016.
 */
public class PointLight {
    private Vector3f intensity;
    private Vector3f position;

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
