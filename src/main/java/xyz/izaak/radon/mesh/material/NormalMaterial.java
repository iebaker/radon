package xyz.izaak.radon.mesh.material;

import xyz.izaak.radon.Resource;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;

public class NormalMaterial extends Material {

    public static final Shader shader = ShaderCompiler.compile(
            "rn.material.Normal",
            Resource.stringFromFile("materials/normal/normal.vert"),
            Resource.stringFromFile("materials/normal/normal.frag"));

    @Override
    public Shader getShader() {
        return shader;
    }
}
