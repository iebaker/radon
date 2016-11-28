package xyz.izaak.radon.math;

import org.joml.Matrix4f;
import xyz.izaak.radon.rendering.shading.Identifiers;
import xyz.izaak.radon.rendering.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.rendering.shading.annotation.ShaderUniform;

/**
 * Created by ibaker on 27/08/2016.
*/
public class MatrixTransformable implements Transformable {
    private Matrix4f model = new Matrix4f();
    private Matrix4f scratch = new Matrix4f();

    @Override
    public void scale(float x, float y, float z) {
        model.set(scratch.scaling(x, y, z).mul(model));
    }

    @Override
    public void translate(float x, float y, float z) {
        model.set(scratch.translation(x, y, z).mul(model));
    }

    @Override
    public void rotate(float amount, float x, float y, float z) {
        model.set(scratch.rotation(amount, x, y, z).mul(model));
    }

    @Override
    public void clearTransforms() {
        model.identity();
    }

    public Matrix4f getModel() {
        return model;
    }
}
