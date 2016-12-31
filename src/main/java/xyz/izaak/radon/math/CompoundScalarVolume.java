package xyz.izaak.radon.math;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ibaker on 08/12/2016.
 */
public class CompoundScalarVolume implements ScalarVolume {
    private List<ScalarVolume> components = new LinkedList<>();
    private List<Reducer> reducers = new LinkedList<>();

    CompoundScalarVolume(ScalarVolume first, Reducer reducer, ScalarVolume second) {
        components.add(first);
        components.add(second);
        reducers.add(reducer);
    }

    @FunctionalInterface
    public interface Reducer {
        float reduce(float a, float b);
    }

    @Override
    public float sample(float x, float y, float z) {
        float result = components.get(0).sample(x, y, z);
        for (int i = 1; i < components.size(); i++) {
            result = reducers.get(i - 1).reduce(result, components.get(i).sample(x, y, z));
        }
        return result;
    }

    @Override
    public CompoundScalarVolume then(Reducer reducer, ScalarVolume other) {
        reducers.add(reducer);
        components.add(other);
        return this;
    }
}
