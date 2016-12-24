package xyz.izaak.radon.mesh.material;

import xyz.izaak.radon.mesh.MeshBuilder;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.UniformProvider;

public abstract class Material implements MeshBuilder, UniformProvider {
    public abstract Shader getShader();
}
