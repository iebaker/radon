package xyz.izaak.radon.primitive;

import xyz.izaak.radon.math.ScalarVolume;

/**
 * Created by ibaker on 30/11/2016.
 */
public class IsosurfacePrimitive extends Primitive {
    private ScalarVolume scalarVolume;
    private float threshold;

    public IsosurfacePrimitive(ScalarVolume scalarVolume, float threshold) {
        this.scalarVolume = scalarVolume;
        this.threshold = threshold;
    }

    @Override
    public void build() {

    }
}
