package xyz.izaak.radon.rendering;

import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Created by ibaker on 17/08/2016.
 */
public class Shader {

    private int stride;
    private int program;
    private List<VertexAttribute> vertexAttributes;
    private String vertexSource;
    private String fragmentSource;

    public Shader(int program, List<VertexAttribute> vertexAttributes, String vertexSource, String fragmentSource) {
        this.program = program;
        this.vertexAttributes = vertexAttributes;
        this.stride = vertexAttributes.stream().collect(Collectors.summingInt(VertexAttribute::getLength));
        this.vertexSource = vertexSource;
        this.fragmentSource = fragmentSource;
    }

    public List<VertexAttribute> getVertexAttributes() {
        return vertexAttributes;
    }

    public void use() {
        glUseProgram(program);
    }

    public int getStride() {
        return stride;
    }

    public String getVertexSource() {
        return vertexSource;
    }

    public String getFragmentSource() {
        return fragmentSource;
    }
}
