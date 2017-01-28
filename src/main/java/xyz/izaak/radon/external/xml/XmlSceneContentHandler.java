package xyz.izaak.radon.external.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.izaak.radon.external.xml.XmlSceneLoader.TARGET_ATTRIBUTE_QNAME;

/**
 * Created by ibaker on 12/01/2017.
 */
public class XmlSceneContentHandler extends DefaultHandler {
    List<XmlElementMapper> xmlElementMappers;
    Deque<XmlElementMapper> mapperStack = new ArrayDeque<>();
    Deque<String> targetStack = new ArrayDeque<>();
    Object result;

    public XmlSceneContentHandler(List<XmlElementMapper> xmlElementMappers) {
        this.xmlElementMappers = xmlElementMappers;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        String[] tokens = qName.split(":");
        String namespace = tokens[0];
        String element = tokens[1];
        xmlElementMappers.forEach(xmlElementMapper -> {
            if (xmlElementMapper.getNamespace().equals(namespace) && xmlElementMapper.getElement().equals(element)) {
                mapperStack.addFirst(xmlElementMapper);
                Map<String, String> rawAttributes = new HashMap<>();
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (attributes.getQName(i).equals(TARGET_ATTRIBUTE_QNAME)) {
                        targetStack.addFirst(attributes.getValue(i));
                    } else {
                        rawAttributes.put(attributes.getQName(i), attributes.getValue(i));
                        targetStack.addFirst("");
                    }
                }
                xmlElementMapper.handleAttributes(rawAttributes);
            }
        });
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        XmlElementMapper xmlElementMapper = mapperStack.removeFirst();
        String target = targetStack.removeFirst();
        Object produced = xmlElementMapper.get();
        if (!mapperStack.isEmpty()) {
            mapperStack.peekFirst().handleChild(produced, target);
        } else {
            result = produced;
        }
    }

    public Object result() {
        return result;
    }
}
