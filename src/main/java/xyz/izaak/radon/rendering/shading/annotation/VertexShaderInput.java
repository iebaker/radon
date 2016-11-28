package xyz.izaak.radon.rendering.shading.annotation;

import xyz.izaak.radon.rendering.shading.ShaderVariableType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Creates an input (attribute) to a vertex shader
 *
 * Created by ibaker on 27/11/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(VertexShaderInputs.class)
@Target(ElementType.TYPE)
@Inherited
public @interface VertexShaderInput {
    ShaderVariableType type();
    String identifier();
}
