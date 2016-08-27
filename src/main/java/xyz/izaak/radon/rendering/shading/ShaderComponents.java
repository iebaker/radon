package xyz.izaak.radon.rendering.shading;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ibaker on 17/08/2016.
 */
public class ShaderComponents {

    private Set<TypedShaderVariable> fragmentIns = new HashSet<>();
    private Set<TypedShaderVariable> uniforms = new HashSet<>();
    private Set<TypedShaderVariable> fragmentOuts = new HashSet<>();
    private Set<String> vertexShaderBlocks = new HashSet<>();
    private Set<String> fragmentShaderBlocks = new HashSet<>();

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

    public void addFragmentIn(ShaderVariableType type, String name) {
        fragmentIns.add(new TypedShaderVariable(type, name));
    }

    public void addUniform(ShaderVariableType type, String name, UniformStore store) {
        TypedShaderVariable variable = new TypedShaderVariable(type, name);
        uniforms.add(variable);
        store.storeUniform(variable);
    }

    public void addFragmentOut(ShaderVariableType type, String name) {
        fragmentOuts.add(new TypedShaderVariable(type, name));
    }

    public void addVertexShaderBlock(String vertexShaderBlock) {
        vertexShaderBlocks.add(vertexShaderBlock);
    }

    public void addFragmentShaderBlock(String fragmentShaderBlock) {
        fragmentShaderBlocks.add(fragmentShaderBlock);
    }

    public Set<TypedShaderVariable> getFragmentIns() {
        return fragmentIns;
    }

    public Set<TypedShaderVariable> getUniforms() {
        return uniforms;
    }

    public Set<TypedShaderVariable> getFragmentOuts() {
        return fragmentOuts;
    }

    public Set<String> getVertexShaderBlocks() {
        return vertexShaderBlocks;
    }

    public Set<String> getFragmentShaderBlocks() {
        return fragmentShaderBlocks;
    }

    public void joinWith(ShaderComponents shaderComponents) {
        this.fragmentIns.addAll(shaderComponents.getFragmentIns());
        this.uniforms.addAll(shaderComponents.getUniforms());
        this.fragmentOuts.addAll(shaderComponents.getFragmentOuts());
        this.vertexShaderBlocks.addAll(shaderComponents.getVertexShaderBlocks());
        this.fragmentShaderBlocks.addAll(shaderComponents.getFragmentShaderBlocks());
    }
}

