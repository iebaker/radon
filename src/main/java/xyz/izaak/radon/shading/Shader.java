package xyz.izaak.radon.shading;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private String vertexSource;
    private String fragmentSource;
    private String name;
    private List<VertexAttribute> vertexAttributes = new ArrayList<>();
    private Map<String, FloatBuffer> uniformStorage = new HashMap<>();
    private Map<String, Integer> uniformLocations = new HashMap<>();
    private Pattern shaderVariableDeclaration = Pattern.compile("(.*)\\s+(.*)\\s+(.*?)(\\[\\d+\\]|);");

    Shader(String name, int program, String vertexSource, String fragmentSource) {
        this.program = program;
        this.vertexSource = vertexSource;
        this.fragmentSource = fragmentSource;
        this.name = name;
        parseVertexAttributes();
        this.stride = vertexAttributes.stream().collect(Collectors.summingInt(VertexAttribute::getLength));
    }

    public void setUniform(String name, int maxLength, List<Vector3f> values) {
        if (!uniformLocations.containsKey(name)) return;
        if (!uniformStorage.containsKey(name)) {
            uniformStorage.put(name, BufferUtils.createFloatBuffer(maxLength * 3));
        }
        int valueCount = values.size();
        for (int i = 0; i < valueCount; i++) {
            uniformStorage.get(name).put(values.get(i).x);
            uniformStorage.get(name).put(values.get(i).y);
            uniformStorage.get(name).put(values.get(i).z);
        }
        uniformStorage.get(name).rewind();
        glUniform3fv(uniformLocations.get(name), uniformStorage.get(name));
    }

    public void setUniform(String name, Matrix3f value) {
        if (!uniformLocations.containsKey(name)) return;
        if (!uniformStorage.containsKey(name)) {
            uniformStorage.put(name, BufferUtils.createFloatBuffer(3 * 3));
        }
        value.get(uniformStorage.get(name));
        glUniformMatrix3fv(uniformLocations.get(name), false, uniformStorage.get(name));
    }

    public void setUniform(String name, Matrix4f value) {
        if (!uniformLocations.containsKey(name)) return;
        if (!uniformStorage.containsKey(name)) {
            uniformStorage.put(name, BufferUtils.createFloatBuffer(4 * 4));
        }
        value.get(uniformStorage.get(name));
        glUniformMatrix4fv(uniformLocations.get(name), false, uniformStorage.get(name));
    }

    public void setUniform(String name, Vector2f value) {
        if (!uniformLocations.containsKey(name)) return;
        if (!uniformStorage.containsKey(name)) {
            uniformStorage.put(name, BufferUtils.createFloatBuffer(2));
        }
        value.get(uniformStorage.get(name));
        glUniform2fv(uniformLocations.get(name), uniformStorage.get(name));
    }

    public void setUniform(String name, Vector3f value) {
        if (!uniformLocations.containsKey(name)) return;
        if (!uniformStorage.containsKey(name)) {
            uniformStorage.put(name, BufferUtils.createFloatBuffer(3));
        }
        value.get(uniformStorage.get(name));
        glUniform3fv(uniformLocations.get(name), uniformStorage.get(name));
    }

    public void setUniform(String name, Vector4f value) {
        if (!uniformLocations.containsKey(name)) return;
        if (!uniformStorage.containsKey(name)) {
            uniformStorage.put(name, BufferUtils.createFloatBuffer(4));
        }
        value.get(uniformStorage.get(name));
        glUniform4fv(uniformLocations.get(name), uniformStorage.get(name));
    }

    public void setUniform(String name, float value) {
        if (!uniformLocations.containsKey(name)) return;
        glUniform1f(uniformLocations.get(name), value);
    }

    public void setUniform(String name, int value) {
        if (!uniformLocations.containsKey(name)) return;
        glUniform1i(uniformLocations.get(name), value);
    }

    public void setUniform(String name, boolean value) {
        if (!uniformLocations.containsKey(name)) return;
        glUniform1i(uniformLocations.get(name), value ? 1 : 0);
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

    public void parseVertexAttributes() {
        int offset = 0;
        String[] vertexShaderLines = vertexSource.split("\\r?\\n");
        for (String line : vertexShaderLines) {
            Matcher matcher = shaderVariableDeclaration.matcher(line);
            if (!matcher.matches()) continue;

            String variableKind = matcher.group(1);
            String variableType = matcher.group(2);
            String variableName = matcher.group(3);

            if (variableKind.equals("in")) {
                switch (variableType) {
                    case "float":
                        vertexAttributes.add(new VertexAttribute(variableName, 1, offset));
                        offset += 1;
                        break;
                    case "vec2":
                        vertexAttributes.add(new VertexAttribute(variableName, 2, offset));
                        offset += 2;
                        break;
                    case "vec3":
                        vertexAttributes.add(new VertexAttribute(variableName, 3, offset));
                        offset += 3;
                        break;
                    case "vec4":
                        vertexAttributes.add(new VertexAttribute(variableName, 4, offset));
                        offset += 4;
                        break;
                    default:
                        throw new IllegalStateException(
                                String.format("Illegal variable type for fragment in: %s", variableType));
                }
            } else if (variableKind.equals("uniform")) {
                int uniformLocation = glGetUniformLocation(program, variableName);
                if (uniformLocation < 0) continue;
                uniformLocations.put(variableName, uniformLocation);
            }
        }

        String[] fragmentShaderLines = fragmentSource.split("\\r?\\n");
        for (String line : fragmentShaderLines) {
            Matcher matcher = shaderVariableDeclaration.matcher(line);
            if (!matcher.matches()) continue;

            String variableKind = matcher.group(1);
            String variableName = matcher.group(3);

            if (variableKind.equals("uniform")) {
                int uniformLocation = glGetUniformLocation(program, variableName);
                if (uniformLocation < 0) continue;
                uniformLocations.put(variableName, uniformLocation);
            }
        }
    }
}
