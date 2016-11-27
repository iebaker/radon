package xyz.izaak.radon.rendering.shading;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static ShaderCompiler instance() {
        return new ShaderCompiler();
    }

    private ShaderCompiler() {
        // Use ShaderCompiler.instance()
    }

    private ShaderComponents shaderComponents = new ShaderComponents();

    public ShaderCompiler with(ShaderComponents shaderComponents) {
        this.shaderComponents.joinWith(shaderComponents);
        return this;
    }

    private String getVertexShaderComponents() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#version 400\n");
        shaderComponents.getVertexIns().forEach(variable ->
            stringBuilder.append(String.format("in %s %s;%n",
                    variable.getType().getTypeString(), variable.getName())));
        stringBuilder.append("\n");
        shaderComponents.getUniforms().forEach(variable ->
            stringBuilder.append(String.format("uniform %s %s;%n",
                    variable.getType().getTypeString(), variable.getName())));
        stringBuilder.append("\n");
        shaderComponents.getVertexOuts().forEach(variable ->
            stringBuilder.append(String.format("out %s %s;%n",
                    variable.getType().getTypeString(), variable.getName())));
        stringBuilder.append("\n");
        shaderComponents.getVertexShaderBlocks().forEach(stringBuilder::append);
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String getFragmentShaderComponents() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#version 400\n");
        shaderComponents.getVertexOuts().forEach(variable ->
            stringBuilder.append(String.format("in %s %s;%n",
                    variable.getType().getTypeString(), variable.getName())));
        stringBuilder.append("\n");
        shaderComponents.getUniforms().forEach(variable ->
            stringBuilder.append(String.format("uniform %s %s;%n",
                    variable.getType().getTypeString(), variable.getName())));
        stringBuilder.append("\n");
        stringBuilder.append("out vec4 fragColor;\n");
        stringBuilder.append("\n");
        shaderComponents.getFragmentShaderBlocks().forEach(stringBuilder::append);
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public Shader compile(String name) throws IOException, RuntimeException {
        int status;

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        String vertexShaderSource = getVertexShaderComponents();
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);

        status = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if(status != GL_TRUE) {
            throw new RuntimeException(glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        String fragmentShaderSource = getFragmentShaderComponents();
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
        for(ShaderComponents.TypedShaderVariable variable : shaderComponents.getVertexIns()) {
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

        Map<ShaderComponents.TypedShaderVariable, UniformStore> uniformStoresByVariable =
                shaderComponents.getUniformStoresByVariable();

        return new Shader(
                name,
                shaderProgram,
                vertexAttributes,
                uniformStoresByVariable,
                vertexShaderSource,
                fragmentShaderSource);
    }
}
