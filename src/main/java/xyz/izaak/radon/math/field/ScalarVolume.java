package xyz.izaak.radon.math.field;

import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Created by ibaker on 01/12/2016.
 */
public interface ScalarVolume {

    float sample(float x, float y, float z);

    default float sample(Vector3f index) {
        return sample(index.x, index.y, index.z);
    }

    default void sample(float[][][] storage, Vector3f min, Vector3i count, Vector3f step) {
        float xIndex, yIndex, zIndex;
        for (float x = 0; x < count.x; x++) {
            for (float y = 0; y < count.y; y++) {
                for (float z = 0; z < count.z; z++) {
                    xIndex = min.x + (x * step.x);
                    yIndex = min.y + (y * step.y);
                    zIndex = min.z + (z * step.z);
                    storage[(int)x][(int)y][(int)z] = sample(xIndex, yIndex, zIndex);
                }
            }
        }
    }
}
