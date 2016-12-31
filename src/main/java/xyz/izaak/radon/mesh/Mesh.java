package xyz.izaak.radon.mesh;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.exception.RadonException;
import xyz.izaak.radon.geometry.Geometry;
import xyz.izaak.radon.material.Material;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.UniformProvider;
import xyz.izaak.radon.shading.VertexAttribute;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

public class Mesh extends MatrixTransformable implements UniformProvider {

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

    public interface DerivationFromVector4f extends Function<Vector4f, float[]> { }
    public interface DerivationFromVector3f extends Function<Vector3f, float[]> { }
    public interface DerivationFromVector2f extends Function<Vector2f, float[]> { }
    public interface DerivationFromFloat    extends Function<Float   , float[]> { }

    private Map<String, List<Float>> vertexData = new HashMap<>();
    private Map<String, List<Float>> defaultVertexData = new HashMap<>();
    private Map<Shader, Integer> vertexArrays = new HashMap<>();
    private List<Runnable> derivations = new ArrayList<>();
    private List<Interval> intervals = new ArrayList<>();
    private List<MeshBuilder> meshBuilders = new ArrayList<>();
    private int vertexCount = 0;
    private Geometry geometry;
    private Material material;

    public Mesh(Geometry geometry, Material material, MeshBuilder... otherMeshBuilders) {
        this.geometry = geometry;
        this.material = material;
        this.meshBuilders.add(geometry);
        this.meshBuilders.add(material);
        this.meshBuilders.addAll(Arrays.asList(otherMeshBuilders));
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Material getMaterial() {
        return material;
    }

    public void build() {
        meshBuilders.forEach(meshBuilder -> meshBuilder.build(this));
        derivations.forEach(Runnable::run);
    }

    public void addInterval(int mode, int count) {
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

    public void derive(String outAttributeName, String inAttributeName, DerivationFromVector4f derivation) {
        derivations.add(() -> {
            List<Float> input = vertexData.get(inAttributeName);
            vertexData.put(outAttributeName, new ArrayList<>());
            Vector4f functionInput = new Vector4f();
            int originalDataCount = input.size();
            for (int i = 0; i < originalDataCount; i += 4) {
                functionInput.set(input.get(i), input.get(i + 1), input.get(i + 2), input.get(i + 3));
                for (float f : derivation.apply(functionInput)) {
                    vertexData.get(outAttributeName).add(f);
                }
            }
        });
    }

    public void derive(String outAttributeName, String inAttributeName, DerivationFromVector3f derivation) {
        derivations.add(() -> {
            List<Float> input = vertexData.get(inAttributeName);
            vertexData.put(outAttributeName, new ArrayList<>());
            Vector3f functionInput = new Vector3f();
            int originalDataCount = input.size();
            for (int i = 0; i < originalDataCount; i += 3) {
                functionInput.set(input.get(i), input.get(i + 1), input.get(i + 2));
                for (float f : derivation.apply(functionInput)) {
                    vertexData.get(outAttributeName).add(f);
                }
            }
        });
    }

    public void derive(String outAttributeName, String inAttributeName, DerivationFromVector2f derivation) {
        derivations.add(() -> {
            List<Float> input = vertexData.get(inAttributeName);
            vertexData.put(outAttributeName, new ArrayList<>());
            Vector2f functionInput = new Vector2f();
            int originalDataCount = input.size();
            for (int i = 0; i < originalDataCount; i += 2) {
                functionInput.set(input.get(i), input.get(i + 1));
                for (float f : derivation.apply(functionInput)) {
                    vertexData.get(outAttributeName).add(f);
                }
            }
        });
    }

    public void derive(String outAttributeName, String inAttributeName, DerivationFromFloat derivation) {
        derivations.add(() -> {
            List<Float> input = vertexData.get(inAttributeName);
            vertexData.put(outAttributeName, new ArrayList<>());
            int originalDataCount = input.size();
            for (int i = 0; i < originalDataCount; i++) {
                for (float f : derivation.apply(input.get(i))) {
                    vertexData.get(outAttributeName).add(f);
                }
            }
        });
    }

    public Matrix4f getModel() {
        return super.getModel();
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.MESH_MODEL, getModel());
    }

    public int getVertexArrayFor(Shader shader) {
        return vertexArrays.get(shader);
    }

    private FloatBuffer getVertexDataFor(Shader shader) throws RadonException {
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
                    throw new RadonException(String.format("%s does not have data for attribute '%s' required by shader '%s",
                            getClass().getName(), attributeName, shader.getName()));
                }

                if (values.size() < attributeLength) {
                    throw new RadonException(String.format("%s does not have enough data for attribute '%s' required by shader '%s'",
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

    public void bufferFor(Shader shader) throws RadonException {
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
            if (attributeLocation < 0) {
                continue;
            }
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
