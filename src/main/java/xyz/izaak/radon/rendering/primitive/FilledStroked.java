package xyz.izaak.radon.rendering.primitive;

import org.joml.Vector3f;
import xyz.izaak.radon.rendering.shading.ShaderComponents;
import xyz.izaak.radon.rendering.shading.ShaderVariableType;

/**
 * Created by ibaker on 27/08/2016.
 */
public interface FilledStroked {
    String VERTEX_COLOR = "rn_VertexColor";

    static ShaderComponents provideShaderComponents() {
        ShaderComponents result = new ShaderComponents();
        result.addVertexIn(ShaderVariableType.VEC3, VERTEX_COLOR);
        return result;
    }

    void setFillColor(Vector3f color);
    void setStrokeColor(Vector3f color);
}
