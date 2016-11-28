package xyz.izaak.radon.rendering.shading;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.rendering.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.rendering.shading.annotation.ShaderUniform;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @FunctionalInterface
    interface SetUniform { void set(int uniformLocation); }

    Shader(
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

    public void setUniforms(Object object) throws IllegalArgumentException {
        ProvidesShaderComponents test = object.getClass().getAnnotation(ProvidesShaderComponents.class);
        if (test == null) {
            throw new IllegalArgumentException(
                    String.format("Class %s does not provide shader components", object.getClass().getSimpleName()));
        }

        for (Method targetMethod : object.getClass().getMethods()) {
            ShaderUniform uniform = targetMethod.getAnnotation(ShaderUniform.class);
            if (uniform == null) {
                continue;
            }

            try {
                Method setUniform = Shader.class.getMethod("setUniform", String.class, targetMethod.getReturnType());
                setUniform.invoke(this, uniform.identifier(), targetMethod.invoke(object));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(
                        String.format("Shader class does not support uniform %s of type %s from class %s",
                                uniform.identifier(),
                                targetMethod.getReturnType().getSimpleName(),
                                object.getClass().getSimpleName()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private int getUniformLocation(String name) {
        return glGetUniformLocation(program, name);
    }

    private void setUniformIfLocationExists(String name, SetUniform setUniform) {
        int uniformLocation = getUniformLocation(name);
        if (uniformLocation >= 0) {
            setUniform.set(uniformLocation);
        } else {
            System.out.println("Location not found for uniform " + name);
        }
    }

    public void setUniform(String name, Matrix3f value) {
        setUniformIfLocationExists(name, location -> glUniformMatrix3fv(location, false, Points.floatBufferOf(value)));
    }

    public void setUniform(String name, Matrix4f value) {
        setUniformIfLocationExists(name, location -> {
            System.out.println("Setting uniform " + name);
            System.out.println(value);
            glUniformMatrix4fv(location, false, Points.floatBufferOf(value));
        });
    }

    public void setUniform(String name, Vector2f value) {
        setUniformIfLocationExists(name, location -> glUniform2fv(location, Points.floatBufferOf(value)));
    }

    public void setUniform(String name, Vector3f value) {
        setUniformIfLocationExists(name, location -> glUniform3fv(location, Points.floatBufferOf(value)));
    }

    public void setUniform(String name, Vector4f value) {
        setUniformIfLocationExists(name, location -> glUniform4fv(location, Points.floatBufferOf(value)));
    }

    public void setUniform(String name, float value) {
        setUniformIfLocationExists(name, location -> glUniform1f(location, value));
    }

    public void setUniform(String name, int value) {
        setUniformIfLocationExists(name, location -> glUniform1i(location, value));
    }

    public void setUniform(String name, boolean value) {
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
        glValidateProgram(program);
        int status = glGetProgrami(program, GL_VALIDATE_STATUS);
        if (status != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(program));
        }
    }
}
