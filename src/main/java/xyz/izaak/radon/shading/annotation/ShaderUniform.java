package xyz.izaak.radon.shading.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ibaker on 27/11/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ShaderUniform {
    String identifier();
    int length() default 1;
}
