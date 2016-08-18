package xyz.izaak.radon.rendering;

import xyz.izaak.radon.core.Resource;

import java.io.IOException;
import java.util.ArrayList;
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

    private ShaderComponents shaderComponents = new ShaderComponents();
    private String fragmentShaderFilename;
    private String vertexShaderFilename;

    public ShaderCompiler with(ShaderComponents shaderComponents) {
        this.shaderComponents.joinWith(shaderComponents);
        return this;
    }

    public ShaderCompiler vertexSource(String vertexShaderFilename) {
        this.vertexShaderFilename = vertexShaderFilename;
        return this;
    }

    public ShaderCompiler fragmentSource(String fragmentShaderFilename) {
        this.fragmentShaderFilename = fragmentShaderFilename;
        return this;
    }

    private String getVertexShaderComponents() {
        final StringBuilder stringBuilder = new StringBuilder();
        shaderComponents.getFragmentIns().forEach(variable ->
            stringBuilder.append(String.format("in %s %s;%n", variable.getType(), variable.getName())));
        stringBuilder.append("\n");
        shaderComponents.getUniforms().forEach(variable ->
            stringBuilder.append(String.format("uniform %s %s;%n", variable.getType(), variable.getName())));
        stringBuilder.append("\n");
        shaderComponents.getFragmentOuts().forEach(variable ->
            stringBuilder.append(String.format("out %s %s;%n", variable.getType(), variable.getName())));
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String getFragmentShaderComponents() {
        final StringBuilder stringBuilder = new StringBuilder();
        shaderComponents.getFragmentOuts().forEach(variable ->
            stringBuilder.append(String.format("in %s %s;%n", variable.getType(), variable.getName())));
        stringBuilder.append("\n");
        shaderComponents.getUniforms().forEach(variable ->
            stringBuilder.append(String.format("uniform %s %s;%n", variable.getType(), variable.getName())));
        stringBuilder.append("\n");
        stringBuilder.append("out vec4 fragColor;\n");
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public Shader compile() throws IOException, RuntimeException {
        int status;

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        String vertexShaderSource = Resource.stringFromFile(vertexShaderFilename);
        vertexShaderSource = getVertexShaderComponents().concat(vertexShaderSource);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        status = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if(status != GL_TRUE) {
            throw new RuntimeException(glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        String fragmentShaderSource = Resource.stringFromFile(fragmentShaderFilename);
        fragmentShaderSource = getFragmentShaderComponents().concat(fragmentShaderSource);
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
        for(ShaderComponents.TypedShaderVariable variable : shaderComponents.getFragmentIns()) {
            switch (variable.getType()) {
                case VEC2:
                    vertexAttributes.add(new VertexAttribute(variable.getName(), 2, offset++));
                    break;
                case VEC3:
                    vertexAttributes.add(new VertexAttribute(variable.getName(), 3, offset++));
                    break;
                case VEC4:
                    vertexAttributes.add(new VertexAttribute(variable.getName(), 4, offset++));
                    break;
                default:
                    throw new IllegalStateException(
                            String.format("Illegal variable type for fragment in: %s", variable.getType()));
            }
        }

        return new Shader(shaderProgram, vertexAttributes, vertexShaderSource, fragmentShaderSource);
    }
}
