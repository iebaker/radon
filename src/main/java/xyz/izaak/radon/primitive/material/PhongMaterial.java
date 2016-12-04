package xyz.izaak.radon.primitive.material;

import org.joml.Vector3f;
import xyz.izaak.radon.primitive.Primitive;
import xyz.izaak.radon.primitive.geometry.Geometry;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.ShaderCompiler;
import xyz.izaak.radon.shading.ShaderVariableType;
import xyz.izaak.radon.shading.annotation.FragmentShaderBlock;
import xyz.izaak.radon.shading.annotation.FragmentShaderMain;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;
import xyz.izaak.radon.shading.annotation.VertexShaderMain;
import xyz.izaak.radon.shading.annotation.VertexShaderOutput;
import xyz.izaak.radon.world.Camera;
import xyz.izaak.radon.world.Entity;

@ProvidesShaderComponents(requires = {Geometry.class, Primitive.class, Entity.class})
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
        StringBuilder posDot = new StringBuilder();
        posDot.append("float rn_PosDot(vec3 a, vec3 b) {\n");
        posDot.append("\treturn max(0, dot(a, b));\n");
        posDot.append("}\n");
        return posDot.toString();
    }

    @FragmentShaderMain
    public static String setFragColor() {
        StringBuilder main = new StringBuilder();
        main.append("vec3 rn_Look = normalize(rn_CameraEye - rn_WorldPosition);\n");
        main.append("vec3 rn_LightDirection = normalize(vec3(1, 1, 1));\n");
        main.append("vec3 rn_HalfAngle = normalize(rn_Look + rn_LightDirection);\n");
        main.append("vec3 rn_Diffuse = rn_PosDot(rn_Normal, rn_LightDirection) * rn_DiffuseColor;\n");
        main.append("vec3 rn_Specular = pow(rn_PosDot(rn_Normal, rn_HalfAngle), rn_SpecularExponent) * rn_SpecularColor;\n");
        main.append("vec3 rn_DiffSpec = vec3(1, 1, 1) * (rn_DiffuseCoefficient * rn_Diffuse + rn_SpecularCoefficient * rn_Specular);\n");
        main.append("fragColor = vec4((rn_AmbientCoefficient * rn_AmbientColor) + rn_DiffSpec, 1);\n");
        return main.toString();
    }

    @VertexShaderMain
    public static String setVertexOutputs() {
        StringBuilder main = new StringBuilder();
        main.append("rn_WorldPosition = vec3(rn_EntityModel * rn_PrimitiveModel * vec4(rn_VertexPosition, 1));\n");
        main.append("rn_Normal = vec3(rn_EntityModel * rn_PrimitiveModel * vec4(rn_VertexNormal, 0));\n");
        return main.toString();
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
    public void build(Primitive primitive) { }
}
