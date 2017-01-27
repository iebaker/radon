package xyz.izaak.radon.external.xml.annotation;

/**
 * Created by ibaker on 11/01/2017.
 */
public @interface XmlElement {
    String namespace() default "";
    String element();
}
