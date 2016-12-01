package xyz.izaak.radon.math;

import org.joml.Vector2f;
import org.joml.Vector2i;

/**
 * Created by ibaker on 30/11/2016.
 */
public interface ScalarArea {

    float samplePoint(float x, float y);

    default float samplePoint(Vector2f index) {
        return samplePoint(index.x, index.y);
    }

    default void sampleRegion(float[] storage, Vector2f min, Vector2i samples, Vector2f steps) {

    }

    default void sampleRegion(float[][] storage, Vector2f min, Vector2i samples, Vector2f steps) {

    }
}
