package xyz.izaak.radon.external.xml;

import org.xml.sax.SAXException;
import xyz.izaak.radon.external.SceneLoader;
import xyz.izaak.radon.scene.Node;
import xyz.izaak.radon.scene.Scene;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ibaker on 11/01/2017.
 */
public class XmlSceneLoader implements SceneLoader {
    private List<XmlElementMapper> xmlElementMappers = new LinkedList<>();
    private SAXParser saxParser;
    private String filename;

    public XmlSceneLoader(String filename) {
        this.filename = filename;

        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        addXmlElementMapper(XmlElementMapper.fromAnnotatedClass(Node.class));
    }

    public void addXmlElementMapper(XmlElementMapper xmlElementMapper) {
        xmlElementMappers.add(xmlElementMapper);
    }

    @Override
    public Scene newInstance() {
        XmlSceneContentHandler handler = new XmlSceneContentHandler(xmlElementMappers);
        try {
            saxParser.parse(filename, handler);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
