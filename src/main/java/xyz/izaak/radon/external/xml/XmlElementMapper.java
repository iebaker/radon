package xyz.izaak.radon.external.xml;

import java.util.Map;

/**
 * Created by ibaker on 11/01/2017.
 */
public abstract class XmlElementMapper<T> {
    public abstract void handleAttributes(Map<String, String> rawParameters);
    public abstract void handleChild(Object child, String target);
    public abstract T get();
}
