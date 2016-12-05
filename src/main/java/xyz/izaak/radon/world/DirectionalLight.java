package xyz.izaak.radon.world;

import org.joml.Vector3f;

/**
 * Created by ibaker on 03/12/2016.
 */
public class DirectionalLight {
    private Vector3f intensity;
    private Vector3f direction;

    public DirectionalLight(Vector3f intensity, Vector3f direction) {
        this.intensity = intensity;
        this.direction = direction;
    }

    public Vector3f getIntensity() {
        return intensity;
    }

    public Vector3f getDirection() {
        return direction;
    }
}
