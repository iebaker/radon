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

        System.out.println(name);
        return new Shader(name, shaderProgram, vertexShaderSource, fragmentShaderSource);
    }
}
