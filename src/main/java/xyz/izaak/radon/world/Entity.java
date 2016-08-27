package xyz.izaak.radon.world;

import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.rendering.primitive.Primitive;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ibaker on 22/08/2016.
 */
public class Entity extends MatrixTransformable {
    private Set<Primitive> primitives = new HashSet<>();

    public Set<Primitive> getPrimitives() {
        return primitives;
    }

    public void addPrimitives(Primitive... primitives) {
        this.primitives.addAll(Arrays.asList(primitives));
    }
}
