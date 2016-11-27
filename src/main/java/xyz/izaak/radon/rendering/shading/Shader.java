package xyz.izaak.radon.rendering.shading;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.izaak.radon.math.Points;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2fv;
import static org.lwjgl.opengl.GL20.glUniform3fv;
import static org.lwjgl.opengl.GL20.glUniform4fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
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

    @FunctionalInterface
    interface SetUniform { void set(int uniformLocation); }

    Shader(
            String name,
            int program,
            List<VertexAttribute> vertexAttributes,
            Map<ShaderComponents.TypedShaderVariable, UniformStore> uniformStores,
            String vertexSource,
            String fragmentSource) {
        this.program = program;
        this.vertexAttributes = vertexAttributes;
        this.uniformStores = uniformStores;
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

    private int getUniformLocation(String name) {
        return glGetUniformLocation(program, name);
    }

    private void setUniformIfLocationExists(String name, SetUniform setUniform) {
        int uniformLocation = getUniformLocation(name);
        if (uniformLocation >= 0) {
            setUniform.set(uniformLocation);
        }
    }

    void setUniform(String name, Matrix3f value) {
        setUniformIfLocationExists(name, location -> glUniformMatrix3fv(location, false, Points.floatBufferOf(value)));
    }

    void setUniform(String name, Matrix4f value) {
        setUniformIfLocationExists(name, location -> glUniformMatrix4fv(location, false, Points.floatBufferOf(value)));
    }

    void setUniform(String name, Vector2f value) {
        setUniformIfLocationExists(name, location -> glUniform2fv(location, Points.floatBufferOf(value)));
    }

    void setUniform(String name, Vector3f value) {
        setUniformIfLocationExists(name, location -> glUniform3fv(location, Points.floatBufferOf(value)));
    }

    void setUniform(String name, Vector4f value) {
        setUniformIfLocationExists(name, location -> glUniform4fv(location, Points.floatBufferOf(value)));
    }

    void setUniform(String name, float value) {
        setUniformIfLocationExists(name, location -> glUniform1f(location, value));
    }

    void setUniform(String name, int value) {
        setUniformIfLocationExists(name, location -> glUniform1i(location, value));
    }

    void setUniform(String name, boolean value) {
        setUniformIfLocationExists(name, location -> glUniform1i(location, value ? 1 : 0));
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
