package xyz.izaak.radon.math;

/**
 * Created by ibaker on 01/12/2016.
 */
@FunctionalInterface
public interface AttenuationFunction {
    AttenuationFunction CONSTANT = distance -> distance;
    AttenuationFunction LINEAR = distance -> 1 / distance;
    AttenuationFunction QUADRATIC = distance -> 1 / distance * distance;

    float valueAt(float distance);
}
