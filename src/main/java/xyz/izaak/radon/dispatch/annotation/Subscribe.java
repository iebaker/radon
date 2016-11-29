package xyz.izaak.radon.dispatch.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ibaker on 29/11/2016.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
    String channel();
}
