package xyz.izaak.radon.math;

/**
 * Created by ibaker on 01/12/2016.
 */
@FunctionalInterface
public interface AttenuationFunction {
    AttenuationFunction CONSTANT = distance -> 1;
    AttenuationFunction INVERSE_LINEAR = distance -> 1 / distance;
    AttenuationFunction INVERSE_QUADRATIC = distance -> 1 / distance * distance;

    float valueAt(float distance);
}
