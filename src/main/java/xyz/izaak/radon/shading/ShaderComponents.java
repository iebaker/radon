package xyz.izaak.radon.shading;

import xyz.izaak.radon.Resource;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by ibaker on 17/08/2016.
 */
public class ShaderComponents {

    private Set<TypedShaderVariable> vertexIns = new HashSet<>();
    private Set<TypedShaderVariable> uniforms = new HashSet<>();
    private Set<TypedShaderVariable> vertexOuts = new HashSet<>();
    private List<String> vertexShaderBlocks = new LinkedList<>();
    private List<String> fragmentShaderBlocks = new LinkedList<>();
    private List<String> vertexShaderMain = new LinkedList<>();
    private List<String> fragmentShaderMain = new LinkedList<>();

    public class TypedShaderVariable {
        private ShaderVariableType type;
        private String name;

        public TypedShaderVariable(ShaderVariableType type, String name) {
            this.type = type;
            this.name = name;
        }

        public ShaderVariableType getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    public void addVertexIn(ShaderVariableType type, String name) {
        vertexIns.add(new TypedShaderVariable(type, name));
    }

    public void addUniform(ShaderVariableType type, String name) {
        TypedShaderVariable variable = new TypedShaderVariable(type, name);
        uniforms.add(variable);
    }

    public void addVertexOut(ShaderVariableType type, String name) {
        vertexOuts.add(new TypedShaderVariable(type, name));
    }

    public void addVertexShaderBlock(String vertexShaderBlock) {
        vertexShaderBlocks.add(vertexShaderBlock);
    }

    public void addFragmentShaderBlock(String fragmentShaderBlock) {
        fragmentShaderBlocks.add(fragmentShaderBlock);
    }

    public void addToVertexMain(String vertexMainBlock) {
        vertexShaderMain.add(vertexMainBlock);
    }

    public void addToFragmentMain(String fragmentMainBlock) {
        fragmentShaderMain.add(fragmentMainBlock);
    }

    public Set<TypedShaderVariable> getVertexIns() {
        return vertexIns;
    }

    public Set<TypedShaderVariable> getUniforms() {
        return uniforms;
    }

    public Set<TypedShaderVariable> getVertexOuts() {
        return vertexOuts;
    }

    public List<String> getVertexShaderBlocks() {
        return vertexShaderBlocks;
    }

    public List<String> getFragmentShaderBlocks() {
        return fragmentShaderBlocks;
    }

    public List<String> getVertexShaderMain() {
        return vertexShaderMain;
    }

    public List<String> getFragmentShaderMain() {
        return fragmentShaderMain;
    }

    public void joinWith(ShaderComponents shaderComponents) {
        this.vertexIns.addAll(shaderComponents.getVertexIns());
        this.uniforms.addAll(shaderComponents.getUniforms());
        this.vertexOuts.addAll(shaderComponents.getVertexOuts());
        this.vertexShaderBlocks.addAll(shaderComponents.getVertexShaderBlocks());
        this.fragmentShaderBlocks.addAll(shaderComponents.getFragmentShaderBlocks());
    }
}

