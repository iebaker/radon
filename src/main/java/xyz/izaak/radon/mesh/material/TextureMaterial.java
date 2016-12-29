package xyz.izaak.radon.mesh.material;

import xyz.izaak.radon.Resource;
import xyz.izaak.radon.mesh.texture.Texture;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;

/**
 * Created by ibaker on 28/12/2016.
 */
public class TextureMaterial extends Material {
    private static final Shader shader = ShaderCompiler.compile(
            "rn.material.Texture",
            Resource.stringFromFile("materials/texture/texture.vert"),
            Resource.stringFromFile("materials/texture/texture.frag"));

    private Texture texture;

    public TextureMaterial(Texture texture) {
        this.texture = texture;
    }

    @Override
    public Shader getShader() {
        return shader;
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.TEXTURE_IMAGE, texture.getTexture());
    }
}
