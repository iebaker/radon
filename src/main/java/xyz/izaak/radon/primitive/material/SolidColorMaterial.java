package xyz.izaak.radon.primitive.material;

import org.joml.Vector3f;
import xyz.izaak.radon.primitive.Primitive;
import xyz.izaak.radon.shading.annotation.FragmentShaderMain;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;

import static xyz.izaak.radon.shading.Identifiers.COLOR;

@ProvidesShaderComponents
public class SolidColorMaterial extends Material {
    private Vector3f color;

    @FragmentShaderMain
    public static String setFragColor() {
        return "fragColor = vec4(rn_Color, 1);\n";
    }

    public SolidColorMaterial(Vector3f color) {
        this.color = color;
    }

    @ShaderUniform(identifier = COLOR)
    public Vector3f getColor() {
        return color;
    }

    @Override
    public void build(Primitive primitive) { }
}
