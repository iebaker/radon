package xyz.izaak.radon.material;

import xyz.izaak.radon.Resource;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;

/**
 * Created by ibaker on 19/12/2016.
 */
public class PortalMaterial extends Material {

    public static final Shader shader = ShaderCompiler.compile(
            "rn.material.Portal",
            Resource.stringFromFile("materials/portal/portal.vert"),
            Resource.stringFromFile("materials/portal/portal.frag"));

    @Override
    public Shader getShader() {
        return shader;
    }
}
