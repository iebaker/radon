package xyz.izaak.radon.shading;

import xyz.izaak.radon.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;

/**
 * Created by ibaker on 17/08/2016.
 */
public class ShaderCompiler {
    public static Shader compile(String name, String vertexShaderSource, String fragmentShaderSource) {
        int status;

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        status = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if(status != GL_TRUE) {
            throw new RuntimeException(glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);

        status = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
        if(status != GL_TRUE) {
            throw new RuntimeException(glGetShaderInfoLog(fragmentShader));
        }

        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glBindFragDataLocation(shaderProgram, 0, "fragColor");
        glLinkProgram(shaderProgram);

        status = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if(status != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(shaderProgram));
        }

        glDetachShader(shaderProgram, vertexShader);
        glDetachShader(shaderProgram, fragmentShader);

        int offset = 0;
        final List<VertexAttribute> vertexAttributes = new ArrayList<>();
        String[] vertexShaderLines = vertexShaderSource.split("\\r?\\n");
        for(String line : vertexShaderLines) {
            String[] tokens = line.split("\\s+");
            if (!tokens[0].trim().equals("in")) continue;
            String variableName = tokens[2].trim();
            variableName = variableName.substring(0, variableName.length() - 1);
            switch (tokens[1].trim()) {
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
                            String.format("Illegal variable type for fragment in: %s", tokens[1].trim()));
            }
        }

        return new Shader(name, shaderProgram, vertexAttributes, vertexShaderSource, fragmentShaderSource);
    }
}
