package xyz.izaak.radon.math.field;

import xyz.izaak.radon.math.AttenuationFunction;

/**
 * Created by ibaker on 01/12/2016.
 */
public class ToroidalFalloffVolume implements ScalarVolume {
    private AttenuationFunction attenuation;
    private float radius;

    public ToroidalFalloffVolume(float radius, AttenuationFunction attenuation) {
        this.attenuation = attenuation;
        this.radius = radius;
    }

    @Override
    public float sample(float x, float y, float z) {
        float radialDistance = (float) Math.sqrt(x * x + y * y) - radius;
        return attenuation.valueAt((float) Math.sqrt(radialDistance * radialDistance + z * z));
    }
}
