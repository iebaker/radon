package xyz.izaak.radon.scene;

import org.joml.Vector3f;

/**
 * A light source which (like sunlight, for example) illuminates uniformly in one direction
 */
public class DirectionalLight {
    private Vector3f intensity;
    private Vector3f direction;

    /**
     * @param intensity the RGB intensity (color and brightness) of the light source
     * @param direction the direction in which the light source is facing
     */
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
