package xyz.izaak.radon.material;

import org.joml.Vector3f;
import xyz.izaak.radon.Resource;
import xyz.izaak.radon.external.xml.annotation.XmlElement;
import xyz.izaak.radon.external.xml.annotation.XmlParam;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;

@XmlElement(namespace = "rn", element = "SolidColorMaterial")
public class SolidColorMaterial extends Material {

    private static final Shader shader = ShaderCompiler.compile(
            "rn.material.SolidColor",
            Resource.stringFromFile("materials/solid/solid.vert"),
            Resource.stringFromFile("materials/solid/solid.frag"));

    private Vector3f color;

    @SuppressWarnings("unused")
    public SolidColorMaterial(
            @XmlParam("red") float red,
            @XmlParam("green") float green,
            @XmlParam("blue") float blue) {
        this(new Vector3f(red, green, blue));
    }

    public SolidColorMaterial(Vector3f color) {
        this.color = color;
    }

    @Override
    public Shader getShader() {
        return shader;
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.COLOR, color);
    }
}
