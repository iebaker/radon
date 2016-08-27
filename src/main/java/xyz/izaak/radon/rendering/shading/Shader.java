package xyz.izaak.radon.rendering.shading;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Created by ibaker on 17/08/2016.
 */
public class Shader {

    private int stride;
    private int program;
    private List<VertexAttribute> vertexAttributes;
    private Map<ShaderComponents.TypedShaderVariable, UniformStore> uniformStores;
    private String vertexSource;
    private String fragmentSource;
    private String name;

    public Shader(
            String name,
            int program,
            List<VertexAttribute> vertexAttributes,
            String vertexSource,
            String fragmentSource) {
        this.program = program;
        this.vertexAttributes = vertexAttributes;
        this.stride = vertexAttributes.stream().collect(Collectors.summingInt(VertexAttribute::getLength));
        this.vertexSource = vertexSource;
        this.fragmentSource = fragmentSource;
        this.name = name;
    }

    public void setUniforms() {
        uniformStores.entrySet().forEach(entry -> {
            UniformStore uniformStore = entry.getValue();
            uniformStore.setOn(this, entry.getKey());
        });
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(program, name);
    }

    public void setUniform(String name, Matrix3f value) {

    }

    public void setUniform(String name, Matrix4f value) {

    }

    public void setUniform(String name, Vector2f value) {

    }

    public void setUniform(String name, Vector3f value) {

    }

    public void setUniform(String name, Vector4f value) {

    }

    public void setUniform(String name, float value) {

    }

    public void setUniform(String name, int value) {

    }

    public void setUniform(String name, boolean value) {

    }

    public List<VertexAttribute> getVertexAttributes() {
        return vertexAttributes;
    }

    public void use() {
        glUseProgram(program);
    }

    public int getStride() {
        return stride;
    }

    public int getProgram() {
        return program;
    }

    public String getName() {
        return name;
    }

    public String getVertexSource() {
        return vertexSource;
    }

    public String getFragmentSource() {
        return fragmentSource;
    }

    public void validate() {

    }
}
