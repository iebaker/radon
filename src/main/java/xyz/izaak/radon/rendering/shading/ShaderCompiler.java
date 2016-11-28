package xyz.izaak.radon.rendering.shading;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.izaak.radon.rendering.shading.annotation.FragmentShaderBlock;
import xyz.izaak.radon.rendering.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.rendering.shading.annotation.ShaderUniform;
import xyz.izaak.radon.rendering.shading.annotation.VertexShaderBlock;
import xyz.izaak.radon.rendering.shading.annotation.VertexShaderInput;
import xyz.izaak.radon.rendering.shading.annotation.VertexShaderOutput;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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
        shaderVariableTypeByClass.put(Vector2f.class, ShaderVariableType.VEC2);
        shaderVariableTypeByClass.put(Vector3f.class, ShaderVariableType.VEC3);
        shaderVariableTypeByClass.put(Vector4f.class, ShaderVariableType.VEC4);
        shaderVariableTypeByClass.put(Matrix3f.class, ShaderVariableType.MAT3);
        shaderVariableTypeByClass.put(Matrix4f.class, ShaderVariableType.MAT4);
        shaderVariableTypeByClass.put(Boolean.TYPE, ShaderVariableType.BOOL);
        shaderVariableTypeByClass.put(Integer.TYPE, ShaderVariableType.INT);
    }

    private ShaderComponents shaderComponents = new ShaderComponents();
    private Map<Class<?>, ShaderVariableType> shaderVariableTypeByClass = new HashMap<>();

    public ShaderCompiler with(Class<?> providerClass) throws IllegalArgumentException {
        ProvidesShaderComponents test = providerClass.getAnnotation(ProvidesShaderComponents.class);
        if (test == null) {
            String template = "Class %s does not provide shader components";
            throw new IllegalArgumentException(String.format(template, providerClass.getSimpleName()));
        }

        for (VertexShaderInput input : providerClass.getAnnotationsByType(VertexShaderInput.class)) {
            shaderComponents.addVertexIn(input.type(), input.identifier());
        }

        for (VertexShaderOutput output : providerClass.getAnnotationsByType(VertexShaderOutput.class)) {
            shaderComponents.addVertexOut(output.type(), output.identifier());
        }

        for (Method method : providerClass.getMethods()) {
            ShaderUniform uniform = method.getAnnotation(ShaderUniform.class);
            if (uniform != null) {
                ShaderVariableType type = shaderVariableTypeByClass.get(method.getReturnType());
                if (type == null) {
                    String template = "Field %s has invalid Java type %s for shader uniform";
                    throw new IllegalArgumentException(
                            String.format(template, method.getName(), method.getReturnType().getSimpleName())
                    );
                }
                shaderComponents.addUniform(type, uniform.identifier());
            }

            VertexShaderBlock vertexShaderBlock = method.getAnnotation(VertexShaderBlock.class);
            if (vertexShaderBlock != null) {
                try {
                    shaderComponents.addVertexShaderBlock((String)method.invoke(null));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            FragmentShaderBlock fragmentShaderBlock = method.getAnnotation(FragmentShaderBlock.class);
            if (fragmentShaderBlock != null) {
                try {
                    shaderComponents.addFragmentShaderBlock((String)method.invoke(null));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

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
                    vertexAttributes.add(new VertexAttribute(variable.getName(), 2, offset));
                    offset += 2;
                    break;
                case VEC3:
                    vertexAttributes.add(new VertexAttribute(variable.getName(), 3, offset));
                    offset += 3;
                    break;
                case VEC4:
                    vertexAttributes.add(new VertexAttribute(variable.getName(), 4, offset));
                    offset += 4;
                    break;
                default:
                    throw new IllegalStateException(
                            String.format("Illegal variable type for fragment in: %s", variable.getType()));
            }
        }

        return new Shader(
                name,
                shaderProgram,
                vertexAttributes,
                vertexShaderSource,
                fragmentShaderSource);
    }
}
