package xyz.izaak.radon.primitive;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.exception.RenderingException;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.ShaderVariableType;
import xyz.izaak.radon.shading.VertexAttribute;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;
import xyz.izaak.radon.shading.annotation.VertexShaderInput;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Created by ibaker on 17/08/2016.
 */
@ProvidesShaderComponents
@VertexShaderInput(type = ShaderVariableType.VEC3, identifier = Identifiers.VERTEX_POSITION)
public abstract class Primitive extends MatrixTransformable {

    private static final int FLOAT_SIZE = 4;

    public class Interval {
        public int mode;
        public int first;
        public int count;

        public Interval(int mode, int first, int count) {
            this.mode = mode;
            this.first = first;
            this.count = count;
        }
    }

    private Map<String, List<Float>> vertexData = new HashMap<>();
    private Map<String, List<Float>> defaultVertexData = new HashMap<>();
    private Map<Shader, Integer> vertexArrays = new HashMap<>();
    private List<Interval> intervals = new ArrayList<>();
    private AxisAngle4f axisAngle = new AxisAngle4f();
    private int vertexCount = 0;

    public abstract void build();

    protected void addInterval(int mode, int count) {
        intervals.add(new Interval(mode, vertexCount, count));
        vertexCount += count;
    }

    public List<Interval> getIntervals() {
        return intervals;
    }

    public void next(int count, String attributeName, float... values) {
        vertexData.putIfAbsent(attributeName, new ArrayList<>());
        List<Float> vertexDataArray = vertexData.get(attributeName);
        for(int i = 0; i < count; i++) {
            for (float value : values) {
                vertexDataArray.add(value);
            }
        }
    }

    public void next(String attributeName, float... values) {
        next(1, attributeName, values);
    }

    public void next(String attributeName, Vector2f value) {
        next(1, attributeName, value.x, value.y);
    }

    public void next(String attributeName, Vector3f value) {
        next(1, attributeName, value.x, value.y, value.z);
    }

    public void next(String attributeName, Vector4f value) {
        next(1, attributeName, value.x, value.y, value.z, value.w);
    }

    public void next(int count, String attributeName, Vector2f value) {
        next(count, attributeName, value.x, value.y);
    }

    public void next(int count, String attributeName, Vector3f value) {
        next(count, attributeName, value.x, value.y, value.z);
    }

    public void next(int count, String attributeName, Vector4f value) {
        next(count, attributeName, value.x, value.y, value.z, value.w);
    }

    public void range(int start, int count, String attributeName, float... values) {
        vertexData.putIfAbsent(attributeName, new ArrayList<>());
        List<Float> vertexDataArray = vertexData.get(attributeName);
        int arity = values.length;

        if (vertexDataArray.size() % arity != 0) {
            throw new IllegalArgumentException(
                    String.format("Attribute %s does not have arity %d", attributeName, arity));
        }

        int vertices = vertexDataArray.size() / arity;
        if (start > vertices) {
            throw new IllegalArgumentException(
                    String.format("Attribute range start %d greater than array length %d", start, vertices));
        }

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < values.length; j++) {
                vertexDataArray.add(((start + i) * arity) + j, values[j]);
            }
        }
    }

    public void range(int start, int count, String attributeName, Vector2f value) {
        range(start, count, attributeName, value.x, value.y);
    }

    public void range(int start, int count, String attributeName, Vector3f value) {
        range(start, count, attributeName, value.x, value.y, value.z);
    }

    public void range(int start, int count, String attributeName, Vector4f value) {
        range(start, count, attributeName, value.x, value.y, value.z, value.w);
    }

    public void all(String attributeName, float... values) {
        defaultVertexData.putIfAbsent(attributeName, new ArrayList<>());
        List<Float> defaultVertexDataArray = defaultVertexData.get(attributeName);
        defaultVertexDataArray.clear();
        for (float value : values) {
            defaultVertexDataArray.add(value);
        }
    }

    public void all(String attributeName, Vector2f value) {
        all(attributeName, value.x, value.y);
    }

    public void all(String attributeName, Vector3f value) {
        all(attributeName, value.x, value.y, value.z);
    }

    public void all(String attributeName, Vector4f value) {
        all(attributeName, value.x, value.y, value.z, value.w);
    }

    @ShaderUniform(identifier = Identifiers.PRIMITIVE_MODEL)
    public Matrix4f getModel() {
        return super.getModel();
    }

    public int getVertexArrayFor(Shader shader) {
        return vertexArrays.get(shader);
    }

    private FloatBuffer getVertexDataFor(Shader shader) throws RenderingException {
        int bufferSize = vertexCount * shader.getStride();
        float data[] = new float[bufferSize];

        List<VertexAttribute> attributes = shader.getVertexAttributes();
        attributes.sort((va1, va2) -> {
            Integer offset1 = va1.getOffset();
            Integer offset2 = va2.getOffset();
            return offset1.compareTo(offset2);
        });

        int index = 0;
        int attributeLength;
        String attributeName;
        List<Float> values;
        for (int i = 0; i < vertexCount; i++) {
            for (VertexAttribute attribute : attributes) {
                attributeName = attribute.getName();
                attributeLength = attribute.getLength();

                if (defaultVertexData.containsKey(attributeName)) {
                    values = defaultVertexData.get(attributeName);
                } else if (vertexData.containsKey(attributeName)) {
                    values = vertexData.get(attributeName);
                } else {
                    throw new RenderingException(String.format("%s does not have data for attribute '%s' required by shader '%s",
                            getClass().getName(), attributeName, shader.getName()));
                }

                if (values.size() < attributeLength) {
                    throw new RenderingException(String.format("%s does not have enough data for attribute '%s' required by shader '%s'",
                            getClass().getName(), attributeName, shader.getName()));
                }

                for (int j = 0; j < attributeLength; j++) {
                    float datum = values.remove(0);
                    data[index++] = datum;
                    values.add(datum);
                }
            }
        }

        FloatBuffer dataBuffer = BufferUtils.createFloatBuffer(bufferSize);
        dataBuffer.put(data);
        dataBuffer.flip();
        return dataBuffer;
    }

    public void bufferFor(Shader shader) throws RenderingException {
        if (vertexArrays.containsKey(shader)) {
            return;
        }

        int vertexArray = glGenVertexArrays();
        vertexArrays.put(shader, vertexArray);
        glBindVertexArray(vertexArray);

        build();
        FloatBuffer vertexData = getVertexDataFor(shader);

        int vertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);

        for (VertexAttribute attribute : shader.getVertexAttributes()) {
            int attributeLocation = glGetAttribLocation(shader.getProgram(), attribute.getName());
            glEnableVertexAttribArray(attributeLocation);
            glVertexAttribPointer(
                    attributeLocation,
                    attribute.getLength(),
                    GL_FLOAT,
                    false,
                    FLOAT_SIZE * shader.getStride(),
                    FLOAT_SIZE * attribute.getOffset());
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
}
