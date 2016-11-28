package xyz.izaak.radon.world;

import org.joml.Matrix4f;
import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.rendering.primitive.Primitive;
import xyz.izaak.radon.rendering.shading.Identifiers;
import xyz.izaak.radon.rendering.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.rendering.shading.annotation.ShaderUniform;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ibaker on 22/08/2016.
 */
@ProvidesShaderComponents
public class Entity extends MatrixTransformable {
    private Set<Primitive> primitives = new HashSet<>();

    public Set<Primitive> getPrimitives() {
        return primitives;
    }

    public void addPrimitives(Primitive... primitives) {
        this.primitives.addAll(Arrays.asList(primitives));
    }

    @ShaderUniform(identifier = Identifiers.ENTITY_MODEL)
    public Matrix4f getModel() {
        return super.getModel();
    }
}
