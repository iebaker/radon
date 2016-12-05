package xyz.izaak.radon.shading;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xyz.izaak.radon.primitive.Primitive;
import xyz.izaak.radon.primitive.geometry.Geometry;
import xyz.izaak.radon.shading.annotation.FragmentShaderBlock;
import xyz.izaak.radon.shading.annotation.FragmentShaderMain;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;
import xyz.izaak.radon.shading.annotation.VertexShaderBlock;
import xyz.izaak.radon.shading.annotation.VertexShaderInput;
import xyz.izaak.radon.shading.annotation.VertexShaderMain;
import xyz.izaak.radon.shading.annotation.VertexShaderOutput;
import xyz.izaak.radon.world.Camera;
import xyz.izaak.radon.world.Entity;
import xyz.izaak.radon.world.Scene;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static ShaderCompiler blankInstance() {
        return new ShaderCompiler();
    }

    public static ShaderCompiler standardInstance() {
        return blankInstance()
                .with(Camera.class)
                .with(Entity.class)
                .with(Primitive.class)
                .with(Geometry.class)
                .with(Scene.class);
    }

    private ShaderCompiler() {
        shaderVariableTypeByClass.put(Vector2f.class, ShaderVariableType.VEC2);
        shaderVariableTypeByClass.put(Vector3f.class, ShaderVariableType.VEC3);
        shaderVariableTypeByClass.put(Vector4f.class, ShaderVariableType.VEC4);
        shaderVariableTypeByClass.put(Matrix3f.class, ShaderVariableType.MAT3);
        shaderVariableTypeByClass.put(Matrix4f.class, ShaderVariableType.MAT4);
        shaderVariableTypeByClass.put(Boolean.TYPE, ShaderVariableType.BOOL);
        shaderVariableTypeByClass.put(Integer.TYPE, ShaderVariableType.INT);
        shaderVariableTypeByClass.put(Float.TYPE, ShaderVariableType.FLOAT);
    }

    private ShaderComponents shaderComponents = new ShaderComponents();
    private Set<Class<?>> providerClasses = new HashSet<>();
    private Map<Class<?>, ShaderVariableType> shaderVariableTypeByClass = new HashMap<>();

    public ShaderCompiler with(Class<?> providerClass) throws IllegalArgumentException {
        ProvidesShaderComponents test = providerClass.getAnnotation(ProvidesShaderComponents.class);
        if (test == null) {
            String template = "Class %s does not provide shader components";
            throw new IllegalArgumentException(String.format(template, providerClass.getSimpleName()));
        }

        providerClasses.add(providerClass);

        for (VertexShaderInput input : providerClass.getAnnotationsByType(VertexShaderInput.class)) {
            shaderComponents.addVertexIn(input.type(), input.identifier());
        }

        for (VertexShaderOutput output : providerClass.getAnnotationsByType(VertexShaderOutput.class)) {
            shaderComponents.addVertexOut(output.type(), output.identifier());
        }

        for (Method method : providerClass.getMethods()) {
            ShaderUniform uniform = method.getAnnotation(ShaderUniform.class);
            if (uniform != null) {
                Class<?> javaType = method.getReturnType();
                if (uniform.length() > 1) {
                    if (!Collection.class.isAssignableFrom(javaType)) {
                        String template = "Uniform %s has declared length %d" +
                                "but provider method %s has invalid Java type %s";
                        throw new IllegalArgumentException(String.format(
                                template,
                                uniform.identifier(),
                                uniform.length(),
                                method.getName(),
                                method.getReturnType().getSimpleName()));
                    } else {
                        ParameterizedType collectionType = (ParameterizedType) method.getGenericReturnType();
                        javaType = (Class<?>) collectionType.getActualTypeArguments()[0];
                    }
                }
                ShaderVariableType type = shaderVariableTypeByClass.get(javaType);
                if (type == null) {
                    String template = "Method %s has invalid Java type %s for shader uniform %s";
                    throw new IllegalArgumentException(
                            String.format(
                                    template,
                                    method.getName(),
                                    javaType,
                                    uniform.identifier()));
                }
                shaderComponents.addUniform(type, uniform.identifier(), uniform.length());
            }

            try {
                VertexShaderBlock vertexShaderBlock = method.getAnnotation(VertexShaderBlock.class);
                if (vertexShaderBlock != null) {
                    shaderComponents.addVertexShaderBlock((String)method.invoke(null));
                }

                FragmentShaderBlock fragmentShaderBlock = method.getAnnotation(FragmentShaderBlock.class);
                if (fragmentShaderBlock != null) {
                    shaderComponents.addFragmentShaderBlock((String)method.invoke(null));
                }

                VertexShaderMain vertexShaderMain = method.getAnnotation(VertexShaderMain.class);
                if (vertexShaderMain != null) {
                    shaderComponents.addToVertexMain((String)method.invoke(null));
                }

                FragmentShaderMain fragmentShaderMain = method.getAnnotation(FragmentShaderMain.class);
                if (fragmentShaderMain != null) {
                    shaderComponents.addToFragmentMain((String)method.invoke(null));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
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
            stringBuilder.append(String.format("uniform %s %s%s;%n",
                    variable.getType().getTypeString(),
                    variable.getName(),
                    variable.getLength() > 1 ? "[" + variable.getLength() + "]" : "")));
        stringBuilder.append("\n");

        shaderComponents.getVertexOuts().forEach(variable ->
            stringBuilder.append(String.format("out %s %s;%n",
                    variable.getType().getTypeString(), variable.getName())));
        stringBuilder.append("\n");

        shaderComponents.getVertexShaderBlocks().forEach(stringBuilder::append);
        stringBuilder.append("\n");

        List<String> vertexShaderMain = shaderComponents.getVertexShaderMain();
        if (!vertexShaderMain.isEmpty()) {
            stringBuilder.append("void main() {\n");
            vertexShaderMain.forEach(line -> stringBuilder.append(String.format("\t%s%n", line.trim())));
            stringBuilder.append("}\n");
        }
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
            stringBuilder.append(String.format("uniform %s %s%s;%n",
                    variable.getType().getTypeString(),
                    variable.getName(),
                    variable.getLength() > 1 ? "[" + variable.getLength() + "]" : "")));

        stringBuilder.append("\n");
        stringBuilder.append("out vec4 fragColor;\n");
        stringBuilder.append("\n");

        shaderComponents.getFragmentShaderBlocks().forEach(stringBuilder::append);
        stringBuilder.append("\n");

        List<String> fragmentShaderMain = shaderComponents.getFragmentShaderMain();
        if (!fragmentShaderMain.isEmpty()) {
            stringBuilder.append("void main() {\n");
            fragmentShaderMain.forEach(line -> stringBuilder.append(String.format("\t%s%n", line.trim())));
            stringBuilder.append("}\n");
        }
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    public void printSource() {
        String vertexShaderSource = getVertexShaderComponents();
        String fragmentShaderSource = getFragmentShaderComponents();
        System.out.println("VERTEX SHADER");
        System.out.println("-------------");
        System.out.print(vertexShaderSource);
        System.out.println("FRAGMENT SHADER");
        System.out.println("---------------");
        System.out.print(fragmentShaderSource);
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
                fragmentShaderSource,
                providerClasses);
    }
}
