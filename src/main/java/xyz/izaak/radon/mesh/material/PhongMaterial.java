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

    private final float ambientCoefficient = 0.1f;

    private Vector3f ambientColor;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private Vector3f emissiveColor;
    private float specularExponent;

    public PhongMaterial(
            Vector3f ambientColor,
            Vector3f diffuseColor,
            Vector3f specularColor,
            Vector3f emissiveColor,
            float specularExponent) {
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.emissiveColor = emissiveColor;
        this.specularExponent = specularExponent;
    }

    @Override
    public Shader getShader() {
        return shader;
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.AMBIENT_COEFFICIENT, ambientCoefficient);
        shader.setUniform(Identifiers.AMBIENT_COLOR, ambientColor);
    }

    @Override
    public void build(Mesh mesh) {
        mesh.all(Identifiers.VERTEX_DIFFUSE_COLOR, diffuseColor);
        mesh.all(Identifiers.VERTEX_SPECULAR_COLOR, specularColor);
        mesh.all(Identifiers.VERTEX_EMISSIVE_COLOR, emissiveColor);
        mesh.all(Identifiers.VERTEX_SPECULAR_EXPONENT, specularExponent);
    }
}
