package xyz.izaak.radon.mesh.material;

import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.geometry.Geometry;
import xyz.izaak.radon.shading.ShaderCompiler;
import xyz.izaak.radon.shading.annotation.FragmentShaderMain;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.world.Camera;
import xyz.izaak.radon.world.Entity;

/**
 * Created by ibaker on 19/12/2016.
 */
@ProvidesShaderComponents(requires = {Geometry.class, Mesh.class, Entity.class})
public class PortalMaterial extends Material {

    static {
        Camera.compileAndRegisterShader(ShaderCompiler.standardInstance().with(PortalMaterial.class));
    }

    @FragmentShaderMain
    public static String setFragColor() {
        return "fragColor = vec4(1, 0, 0, 1);\n";
    }
}
