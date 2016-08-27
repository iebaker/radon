package xyz.izaak.radon.rendering.shading;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ibaker on 18/08/2016.
 */
public class UniformStore {

    private final Map<String, Matrix3f> matrix3fMap = new HashMap<>();
    private final Map<String, Matrix4f> matrix4fMap = new HashMap<>();
    private final Map<String, Vector2f> vector2fMap = new HashMap<>();
    private final Map<String, Vector3f> vector3fMap = new HashMap<>();
    private final Map<String, Vector4f> vector4fMap = new HashMap<>();
    private final Map<String, Integer> integerMap = new HashMap<>();
    private final Map<String, Float> floatMap = new HashMap<>();
    private final Map<String, Boolean> booleanMap = new HashMap<>();

    public Matrix3f updateUniformMatrix3f(String name) {
        return matrix3fMap.get(name);
    }

    public Matrix4f updateUniformMatrix4f(String name) {
        return matrix4fMap.get(name);
    }

    public Vector2f updateUniformVector2f(String name) {
        return vector2fMap.get(name);
    }

    public Vector3f updateUniformVector3f(String name) {
        return vector3fMap.get(name);
    }

    public Vector4f updateUniformVector4f(String name) {
        return vector4fMap.get(name);
    }

    public void updateUniformInteger(String name, int value) {
        integerMap.put(name, value);
    }

    public void updateUniformFloat(String name, float value) {
        floatMap.put(name, value);
    }

    public void updateUniformBoolean(String name, boolean value) {
        booleanMap.put(name, value);
    }

    public void storeUniform(ShaderComponents.TypedShaderVariable uniform) {
        ShaderVariableType type = uniform.getType();
        String name = uniform.getName();
        switch (type) {
            case FLOAT:
                floatMap.put(name, 0.0f);
                break;
            case BOOL:
                booleanMap.put(name, false);
                break;
            case MAT3:
            case MAT3X3:
                matrix3fMap.put(name, new Matrix3f());
                break;
            case MAT4:
            case MAT4X4:
                matrix4fMap.put(name, new Matrix4f());
                break;
            case VEC2:
                vector2fMap.put(name, new Vector2f());
                break;
            case VEC3:
                vector3fMap.put(name, new Vector3f());
                break;
            case VEC4:
                vector4fMap.put(name, new Vector4f());
                break;
            default:
                integerMap.put(name, 0);
                break;
        }
    }

    public void setOn(Shader shader, ShaderComponents.TypedShaderVariable uniform) {
        ShaderVariableType type = uniform.getType();
        String name = uniform.getName();
        switch (type) {
            case FLOAT:
                shader.setUniform(name, floatMap.get(name));
                break;
            case BOOL:
                shader.setUniform(name, booleanMap.get(name));
                break;
            case MAT3:
            case MAT3X3:
                shader.setUniform(name, matrix3fMap.get(name));
                break;
            case MAT4:
            case MAT4X4:
                shader.setUniform(name, matrix4fMap.get(name));
                break;
            case VEC2:
                shader.setUniform(name, vector2fMap.get(name));
                break;
            case VEC3:
                shader.setUniform(name, vector3fMap.get(name));
                break;
            case VEC4:
                shader.setUniform(name, vector4fMap.get(name));
                break;
            default:
                shader.setUniform(name, integerMap.get(name));
                break;
        }
    }
}
