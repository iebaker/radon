package xyz.izaak.radon.primitive;

import org.joml.Vector3f;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.ShaderVariableType;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.VertexShaderInput;

/**
 * Created by ibaker on 27/08/2016.
 */
@ProvidesShaderComponents
@VertexShaderInput(type = ShaderVariableType.VEC3, identifier = Identifiers.VERTEX_COLOR)
public interface FilledStroked {
    void setFillColor(Vector3f color);
    void setStrokeColor(Vector3f color);
}
