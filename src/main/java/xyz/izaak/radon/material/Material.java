package xyz.izaak.radon.material;

import xyz.izaak.radon.scene.MeshBuilder;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.UniformProvider;

public abstract class Material implements MeshBuilder, UniformProvider {
    public abstract Shader getShader();
}
