package xyz.izaak.radon.external.xml;

import org.xml.sax.SAXException;
import xyz.izaak.radon.Resource;
import xyz.izaak.radon.external.SceneLoader;
import xyz.izaak.radon.geometry.PolarSphereGeometry;
import xyz.izaak.radon.material.SolidColorMaterial;
import xyz.izaak.radon.scene.Mesh;
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
    public static final String TARGET_ATTRIBUTE_QNAME = "rn:target";

    private List<XmlElementMapperFactory> xmlElementMapperFactories = new LinkedList<>();
    private SAXParser saxParser;
    private String filename;

    public XmlSceneLoader(String filename) {
        this.filename = filename;

        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        addFactory(XmlElementMapperFactory.forClass(Node.class));
        addFactory(XmlElementMapperFactory.forClass(Mesh.class));
        addFactory(XmlElementMapperFactory.forClass(PolarSphereGeometry.class));
        addFactory(XmlElementMapperFactory.forClass(SolidColorMaterial.class));
    }

    public void addFactory(XmlElementMapperFactory xmlElementMapperFactory) {
        xmlElementMapperFactories.add(xmlElementMapperFactory);
    }

    @Override
    public Scene newInstance() {
        XmlContentHandler handler = new XmlContentHandler(xmlElementMapperFactories);
        try {
            saxParser.parse(Resource.streamFromFile(filename), handler);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            return null;
        }
        Node root = (Node) handler.result();
        return Scene.builder().root(root).build();
    }
}
