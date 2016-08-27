package xyz.izaak.radon.rendering.shading;

/**
 * Created by ibaker on 17/08/2016.
 */
public class VertexAttribute {
    private final String name;
    private final int length;
    private final int offset;

    public VertexAttribute(String name, int length, int offset) {
        this.name = name;
        this.length = length;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }
}
