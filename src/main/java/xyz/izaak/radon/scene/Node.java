package xyz.izaak.radon.scene;

import xyz.izaak.radon.external.xml.annotation.XmlChild;
import xyz.izaak.radon.external.xml.annotation.XmlElement;
import xyz.izaak.radon.external.xml.annotation.XmlParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ibaker on 08/01/2017.
 */
@XmlElement(namespace = "rn", element = "Node")
public class Node {
    private Map<String, Node> children = new HashMap<>();
    private String name;

    public Node(@XmlParam("name") String name) {
        this.name = name;
    }

    @XmlChild
    public void addChild(Node child) {
        children.put(child.getName(), child);
    }

    public String getName() {
        return name;
    }
}
