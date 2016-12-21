package xyz.izaak.radon.mesh.material;

import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.geometry.Geometry;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.ShaderCompiler;
import xyz.izaak.radon.shading.ShaderVariableType;
import xyz.izaak.radon.shading.annotation.FragmentShaderMain;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.VertexShaderMain;
import xyz.izaak.radon.shading.annotation.VertexShaderOutput;
import xyz.izaak.radon.world.Camera;
import xyz.izaak.radon.world.Entity;

@ProvidesShaderComponents(requires = {Geometry.class, Mesh.class, Entity.class})
@VertexShaderOutput(type = ShaderVariableType.VEC3, identifier = Identifiers.NORMAL)
public class NormalMaterial extends Material {

    static {
        Camera.compileAndRegisterShader(ShaderCompiler.standardInstance().with(NormalMaterial.class));
    }

    @VertexShaderMain
    public static String setVertexOutputs() {
        return "rn_Normal = vec3(rn_EntityModel * rn_MeshModel * vec4(rn_VertexNormal, 0));\n";
    }

    @FragmentShaderMain
    public static String setFragColor() {
        return "fragColor = vec4(vec3(0.5f) + (0.5f * rn_Normal), 1.0f);\n";
    }
}
