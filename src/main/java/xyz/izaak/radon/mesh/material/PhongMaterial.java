package xyz.izaak.radon.mesh.material;

import org.joml.Vector3f;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.mesh.geometry.Geometry;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderCompiler;
import xyz.izaak.radon.shading.ShaderVariableType;
import xyz.izaak.radon.shading.UniformProvider;
import xyz.izaak.radon.shading.annotation.FragmentShaderBlock;
import xyz.izaak.radon.shading.annotation.FragmentShaderMain;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;
import xyz.izaak.radon.shading.annotation.VertexShaderMain;
import xyz.izaak.radon.shading.annotation.VertexShaderOutput;
import xyz.izaak.radon.world.Camera;
import xyz.izaak.radon.world.Entity;
import xyz.izaak.radon.world.Scene;

@ProvidesShaderComponents(requires = {Geometry.class, Mesh.class, Entity.class, Scene.class})
@VertexShaderOutput(type = ShaderVariableType.VEC3, identifier = Identifiers.WORLD_POSITION)
@VertexShaderOutput(type = ShaderVariableType.VEC3, identifier = Identifiers.NORMAL)
public class PhongMaterial extends Material {

    static {
        Camera.compileAndRegisterShader(ShaderCompiler.standardInstance().with(PhongMaterial.class));
    }

    private final float ambientCoefficient = 0.2f;
    private final float diffuseCoefficient = 0.5f;
    private final float specularCoefficient = 0.3f;

    private Vector3f ambientColor;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private float specularExponent;

    @FragmentShaderBlock
    public static String positiveDotProduct() {
        return ""
                .concat("float rn_PosDot(vec3 a, vec3 b) {\n")
                .concat("\treturn max(0, dot(a, b));\n")
                .concat("}\n");
    }

    @FragmentShaderMain
    public static String setFragColor() {
        return ""

                // declare light data storage
                .concat("vec3 rn_Direction;\n")
                .concat("vec3 rn_HalfAngle;\n")
                .concat("float rn_DiffuseFactor;\n")
                .concat("vec3 rn_Diffuse;\n")
                .concat("float rn_SpecularFactor;\n")
                .concat("vec3 rn_Specular;\n")
                .concat("float rn_Attenuation;\n")

                // set up final color accumulator, compute look vector
                .concat("vec3 rn_FinalColor = rn_AmbientCoefficient * rn_AmbientColor;\n")
                .concat("vec3 rn_Look = normalize(rn_CameraEye - rn_WorldPosition);\n")

                // collect directional light contributions
                .concat("for (int i = 0; i < rn_DirectionalLightCount; i++) {\n")
                .concat("\trn_Direction = normalize(rn_DirectionalLightDirections[i]);\n")
                .concat("\trn_HalfAngle = normalize(rn_Look + rn_Direction);\n")
                .concat("\trn_DiffuseFactor = rn_DiffuseCoefficient * rn_PosDot(rn_Normal, rn_Direction);\n")
                .concat("\trn_Diffuse = rn_DiffuseFactor * rn_DiffuseColor;\n")
                .concat("\trn_SpecularFactor = rn_SpecularCoefficient * pow(rn_PosDot(rn_Normal, rn_HalfAngle), rn_SpecularExponent);\n")
                .concat("\trn_Specular = rn_SpecularFactor * rn_SpecularColor;\n")
                .concat("\trn_FinalColor += (rn_DirectionalLightIntensities[i] * (rn_Diffuse + rn_Specular));\n")
                .concat("}\n")

                // collect point light contributions
                .concat("for (int i = 0; i < rn_PointLightCount; i++) {\n")
                .concat("\trn_Direction = rn_PointLightPositions[i] - rn_WorldPosition;\n")
                .concat("\trn_Attenuation = 1 / length(rn_Direction);\n")
                .concat("\trn_Direction = normalize(rn_Direction);\n")
                .concat("\trn_HalfAngle = normalize(rn_Look + rn_Direction);\n")
                .concat("\trn_DiffuseFactor = rn_PosDot(rn_Normal, rn_Direction);\n")
                .concat("\trn_Diffuse = rn_DiffuseFactor * rn_DiffuseColor;\n")
                .concat("\trn_SpecularFactor = rn_SpecularCoefficient * pow(rn_PosDot(rn_Normal, rn_HalfAngle), rn_SpecularExponent);\n")
                .concat("\trn_Specular = rn_SpecularFactor * rn_SpecularColor;\n")
                .concat("\trn_FinalColor += rn_Attenuation * rn_PointLightIntensities[i] * (rn_Diffuse + rn_Specular);\n")
                .concat("}\n")

                // set frag color
                .concat("fragColor = vec4(rn_FinalColor, 1.0f);\n");
    }

    @VertexShaderMain
    public static String setVertexOutputs() {
        return ""
                .concat("rn_WorldPosition = vec3(rn_EntityModel * rn_MeshModel * vec4(rn_VertexPosition, 1));\n")
                .concat("rn_Normal = normalize(vec3(rn_EntityModel * rn_MeshModel * vec4(rn_VertexNormal, 0)));\n");
    }

    public PhongMaterial(Vector3f ambientColor, Vector3f diffuseColor, Vector3f specularColor, float specularExponent) {
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.specularExponent = specularExponent;
    }

    @ShaderUniform(identifier = Identifiers.AMBIENT_COEFFICIENT)
    public float getAmbientCoefficient() {
        return ambientCoefficient;
    }

    @ShaderUniform(identifier = Identifiers.DIFFUSE_COEFFICIENT)
    public float getDiffuseCoefficient() {
        return diffuseCoefficient;
    }

    @ShaderUniform(identifier = Identifiers.SPECULAR_COEFFICIENT)
    public float getSpecularCoefficient() {
        return specularCoefficient;
    }

    @ShaderUniform(identifier = Identifiers.SPECULAR_EXPONENT)
    public float getSpecularExponent() {
        return specularExponent;
    }

    @ShaderUniform(identifier = Identifiers.AMBIENT_COLOR)
    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    @ShaderUniform(identifier = Identifiers.DIFFUSE_COLOR)
    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    @ShaderUniform(identifier = Identifiers.SPECULAR_COLOR)
    public Vector3f getSpecularColor() {
        return specularColor;
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.AMBIENT_COEFFICIENT, getAmbientCoefficient());
        shader.setUniform(Identifiers.DIFFUSE_COEFFICIENT, getDiffuseCoefficient());
        shader.setUniform(Identifiers.SPECULAR_COEFFICIENT, getSpecularCoefficient());
        shader.setUniform(Identifiers.SPECULAR_EXPONENT, getSpecularExponent());
        shader.setUniform(Identifiers.AMBIENT_COLOR, getAmbientColor());
        shader.setUniform(Identifiers.DIFFUSE_COLOR, getDiffuseColor());
        shader.setUniform(Identifiers.SPECULAR_COLOR, getSpecularColor());
    }
}
