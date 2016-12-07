package xyz.izaak.radon.math.field;

import xyz.izaak.radon.math.AttenuationFunction;

/**
 * Created by ibaker on 05/12/2016.
 */
public class RadialFalloffVolume implements ScalarVolume {
    private AttenuationFunction attenuation;

    public RadialFalloffVolume(AttenuationFunction attenuation) {
        this.attenuation = attenuation;
    }

    @Override
    public float sample(float x, float y, float z) {
        return attenuation.valueAt((float) Math.sqrt(x * x + y * y + z + z));
    }
}
