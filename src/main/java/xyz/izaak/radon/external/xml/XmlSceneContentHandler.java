package xyz.izaak.radon.external.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import xyz.izaak.radon.scene.Scene;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Created by ibaker on 12/01/2017.
 */
public class XmlSceneContentHandler extends DefaultHandler {
    List<XmlElementMapper> xmlElementMappers;
    Deque<XmlElementMapper> mapperStack = new ArrayDeque<>();
    Object result;

    public XmlSceneContentHandler(List<XmlElementMapper> xmlElementMappers) {
        this.xmlElementMappers = xmlElementMappers;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        System.out.println("Start element");
        System.out.println(uri);
        System.out.println(localName);
        System.out.println(qName);
        System.out.println(attributes);
    }
}
