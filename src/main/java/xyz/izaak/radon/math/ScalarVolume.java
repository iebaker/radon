package xyz.izaak.radon.math;

import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Created by ibaker on 30/11/2016.
 */
public interface ScalarVolume {

    float samplePoint(float x, float y, float z);

    default float samplePoint(Vector3f index) {
        return samplePoint(index.x, index.y, index.z);
    }

    default void sampleVolume(float[] storage, Vector3f min, Vector3i samples, Vector3f steps) {

    }

    default void sampleVolume(float[][][] storage, Vector3f min, Vector3i samples, Vector3f steps) {
        for (int x = 0; x < samples.x; x++) {
            for (int y = 0; y < samples.y; y++) {
                for (int z = 0; z < samples.z; z++) {
                    storage[x][y][z] = samplePoint(min.x + (x * steps.x), min.y + (y * steps.y), min.z + (z * steps.z));
                }
            }
        }
    }
}
