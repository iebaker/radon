package xyz.izaak.radon.shading;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform2fv;
import static org.lwjgl.opengl.GL20.glUniform3fv;
import static org.lwjgl.opengl.GL20.glUniform4fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;

/**
 * Created by ibaker on 17/08/2016.
 */
public class Shader {

    private int stride;
    private int program;
    private List<VertexAttribute> vertexAttributes;
    private String vertexSource;
    private String fragmentSource;
    private String name;
    private Set<Class<?>> providerClasses;
    private Map<String, FloatBuffer> uniformStorage = new HashMap<>();

    @FunctionalInterface
    interface SetUniform { void set(int uniformLocation); }

    Shader(
            String name,
            int program,
            List<VertexAttribute> vertexAttributes,
            String vertexSource,
            String fragmentSource,
            Set<Class<?>> providerClasses) {
        this.program = program;
        this.vertexAttributes = vertexAttributes;
        this.stride = vertexAttributes.stream().collect(Collectors.summingInt(VertexAttribute::getLength));
        this.vertexSource = vertexSource;
        this.fragmentSource = fragmentSource;
        this.name = name;
        this.providerClasses = providerClasses;
    }

    private int getUniformLocation(String name) {
        return glGetUniformLocation(program, name);
    }

    public void setUniform(String name, int maxLength, List<Vector3f> values) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            if (!uniformStorage.containsKey(name)) {
                uniformStorage.put(name, BufferUtils.createFloatBuffer(maxLength * 3));
            }
            int index = 0;
            for (Vector3f value : values) {
                uniformStorage.get(name).put(value.x);
                uniformStorage.get(name).put(value.y);
                uniformStorage.get(name).put(value.z);
            }
            uniformStorage.get(name).rewind();
            glUniform3fv(uniformLocation, uniformStorage.get(name));
        }
    }

    public void setUniform(String name, Matrix3f value) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            if (!uniformStorage.containsKey(name)) {
                uniformStorage.put(name, BufferUtils.createFloatBuffer(3 * 3));
            }
            value.get(uniformStorage.get(name));
            glUniformMatrix3fv(uniformLocation, false, uniformStorage.get(name));
        }
    }

    public void setUniform(String name, Matrix4f value) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            if (!uniformStorage.containsKey(name)) {
                uniformStorage.put(name, BufferUtils.createFloatBuffer(4 * 4));
            }
            value.get(uniformStorage.get(name));
            glUniformMatrix4fv(uniformLocation, false, uniformStorage.get(name));
        }
    }

    public void setUniform(String name, Vector2f value) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            if (!uniformStorage.containsKey(name)) {
                uniformStorage.put(name, BufferUtils.createFloatBuffer(2));
            }
            value.get(uniformStorage.get(name));
            glUniform2fv(uniformLocation, uniformStorage.get(name));
        }
    }

    public void setUniform(String name, Vector3f value) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            if (!uniformStorage.containsKey(name)) {
                uniformStorage.put(name, BufferUtils.createFloatBuffer(3));
            }
            value.get(uniformStorage.get(name));
            glUniform3fv(uniformLocation, uniformStorage.get(name));
        }
    }

    public void setUniform(String name, Vector4f value) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            if (!uniformStorage.containsKey(name)) {
                uniformStorage.put(name, BufferUtils.createFloatBuffer(4));
            }
            value.get(uniformStorage.get(name));
            glUniform4fv(uniformLocation, uniformStorage.get(name));
        }
    }

    public void setUniform(String name, float value) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            glUniform1f(uniformLocation, value);
        }
    }

    public void setUniform(String name, int value) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            glUniform1i(uniformLocation, value);
        }
    }

    public void setUniform(String name, boolean value) {
        int uniformLocation = glGetUniformLocation(program, name);
        if (uniformLocation >= 0) {
            glUniform1i(uniformLocation, value ? 1 : 0);
        }
    }

    public List<VertexAttribute> getVertexAttributes() {
        return vertexAttributes;
    }

    public boolean supports(Class<?> providerClass) {
        return providerClasses.contains(providerClass);
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
        glValidateProgram(program);
        int status = glGetProgrami(program, GL_VALIDATE_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(program));
        }
    }
}
