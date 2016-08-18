package xyz.izaak.radon.rendering;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ibaker on 17/08/2016.
 */
public class Renderable implements ShaderComponentProvider {

    private static ShaderComponents shaderComponents = new ShaderComponents();
    private static final String MODEL = "model";
    private static final String VERTEX_POSITION = "vertexPosition";
    private static final String VERTEX_NORMAL = "vertexNormal";

    static {
        shaderComponents.addUniform(ShaderVariableType.MAT4, MODEL);
        shaderComponents.addFragmentIn(ShaderVariableType.VEC3, VERTEX_POSITION);
        shaderComponents.addFragmentIn(ShaderVariableType.VEC3, VERTEX_NORMAL);
    }

    public static ShaderComponents provideShaderComponents() {
        return shaderComponents;
    }

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

    private Shader shader;
    private Map<String, List<Float>> vertexData = new HashMap<>();
    private Map<String, List<Float>> defaultVertexData = new HashMap<>();
    private List<Interval> intervals = new ArrayList<>();
    private Matrix4f model = new Matrix4f();
    private Matrix4f modifier = new Matrix4f();
    private AxisAngle4f axisAngle = new AxisAngle4f();
    private int vertexCount = 0;

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

    public void scale(float factor) {
        modifier.scaling(factor).mul(model);
    }

    public void scale(float x, float y, float z) {
        modifier.scaling(x, y, z).mul(model);
    }

    public void scale(Vector3f factor) {
        modifier.scaling(factor).mul(model);
    }

    public void translate(float x, float y, float z) {
        modifier.translation(x, y, z).mul(model);
    }

    public void translate(Vector3f distance) {
        modifier.translation(distance).mul(model);
    }

    public void rotate(float amount, Vector3f axis) {
        modifier.rotation(axisAngle.set(amount, axis)).mul(model);
    }

    public void rotate(float amount, float x, float y, float z) {
        modifier.rotation(axisAngle.set(amount, x, y, z)).mul(model);
    }

    public void clearTransforms() {
        model.identity();
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }

    public Shader getShader() {
        return shader;
    }
}
