package xyz.izaak.radon.mesh.material;

import org.joml.Vector3f;
import xyz.izaak.radon.Resource;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;

public class PhongMaterial extends Material {

    private static final Shader shader = ShaderCompiler.compile(
            "rn.material.Phong",
            Resource.stringFromFile("materials/phong/phong.vert"),
            Resource.stringFromFile("materials/phong/phong.frag"));

    private final float ambientCoefficient = 0.2f;
    private final float diffuseCoefficient = 0.5f;
    private final float specularCoefficient = 0.3f;

    private Vector3f ambientColor;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private float specularExponent;

    public PhongMaterial(Vector3f ambientColor, Vector3f diffuseColor, Vector3f specularColor, float specularExponent) {
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.specularExponent = specularExponent;
    }

    @Override
    public Shader getShader() {
        return shader;
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.AMBIENT_COEFFICIENT, ambientCoefficient);
        shader.setUniform(Identifiers.DIFFUSE_COEFFICIENT, diffuseCoefficient);
        shader.setUniform(Identifiers.SPECULAR_COEFFICIENT, specularCoefficient);
        shader.setUniform(Identifiers.AMBIENT_COLOR, ambientColor);
    }

    @Override
    public void build(Mesh mesh) {
        mesh.all(Identifiers.VERTEX_DIFFUSE_COLOR, diffuseColor);
        mesh.all(Identifiers.VERTEX_SPECULAR_COLOR, specularColor);
        mesh.all(Identifiers.VERTEX_SPECULAR_EXPONENT, specularExponent);
    }
}
