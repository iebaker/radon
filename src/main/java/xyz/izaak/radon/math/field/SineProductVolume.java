package xyz.izaak.radon.math.field;

/**
 * Created by ibaker on 08/12/2016.
 */
public class SineProductVolume implements ScalarVolume {
    private float xMultiplier;
    private float yMultiplier;
    private float zScale;

    public SineProductVolume(float xMultiplier, float yMultiplier, float zScale) {
        this.xMultiplier = xMultiplier;
        this.yMultiplier = yMultiplier;
        this.zScale = zScale;
    }

    @Override
    public float sample(float x, float y, float z) {
        return (float) (Math.sin(xMultiplier * x) * Math.sin(yMultiplier * y) * zScale - z);
    }
}
