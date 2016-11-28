package xyz.izaak.radon.rendering.primitive;

import org.joml.Vector3f;
import xyz.izaak.radon.rendering.shading.Identifiers;
import xyz.izaak.radon.rendering.shading.ShaderComponents;
import xyz.izaak.radon.rendering.shading.ShaderVariableType;
import xyz.izaak.radon.rendering.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.rendering.shading.annotation.VertexShaderInput;

/**
 * Created by ibaker on 27/08/2016.
 */
@ProvidesShaderComponents
@VertexShaderInput(type = ShaderVariableType.VEC3, identifier = Identifiers.VERTEX_COLOR)
public interface FilledStroked {
    void setFillColor(Vector3f color);
    void setStrokeColor(Vector3f color);
}
