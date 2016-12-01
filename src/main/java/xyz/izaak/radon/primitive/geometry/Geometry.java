package xyz.izaak.radon.primitive.geometry;

import xyz.izaak.radon.primitive.PrimitiveBuilder;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.VertexShaderInput;

import static xyz.izaak.radon.shading.Identifiers.VERTEX_NORMAL;
import static xyz.izaak.radon.shading.Identifiers.VERTEX_POSITION;
import static xyz.izaak.radon.shading.ShaderVariableType.VEC3;

@ProvidesShaderComponents
@VertexShaderInput(type = VEC3, identifier = VERTEX_POSITION)
@VertexShaderInput(type = VEC3, identifier = VERTEX_NORMAL)
public abstract class Geometry implements PrimitiveBuilder {

}
