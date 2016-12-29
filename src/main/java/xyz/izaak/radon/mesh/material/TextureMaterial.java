package xyz.izaak.radon.mesh.material;

import xyz.izaak.radon.Resource;
import xyz.izaak.radon.mesh.texture.Texture;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

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
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getTexture());
        shader.setUniform(Identifiers.TEXTURE_IMAGE, 0);
    }
}
