package xyz.izaak.radon.primitive;

/**
 * Created by ibaker on 30/11/2016.
 */
public interface PrimitiveBuilder {
    default void build(Primitive primitive) { }
}
