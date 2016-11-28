package xyz.izaak.radon.rendering.shading;

import xyz.izaak.radon.Resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ibaker on 17/08/2016.
 */
public class ShaderComponents {

    private Set<TypedShaderVariable> vertexIns = new HashSet<>();
    private Set<TypedShaderVariable> uniforms = new HashSet<>();
    private Set<TypedShaderVariable> vertexOuts = new HashSet<>();
    private Set<String> vertexShaderBlocks = new HashSet<>();
    private Set<String> fragmentShaderBlocks = new HashSet<>();
    private Map<TypedShaderVariable, UniformStore> uniformStoresByVariable = new HashMap<>();

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
        if (vertexShaderBlock.endsWith(".glsl")) {
            vertexShaderBlocks.add(Resource.stringFromFile(vertexShaderBlock));
        } else {
            vertexShaderBlocks.add(vertexShaderBlock);
        }
    }

    public void addFragmentShaderBlock(String fragmentShaderBlock) {
        if (fragmentShaderBlock.endsWith(".glsl")) {
            fragmentShaderBlocks.add(Resource.stringFromFile(fragmentShaderBlock));
        } else {
            fragmentShaderBlocks.add(fragmentShaderBlock);
        }
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

    public Set<String> getVertexShaderBlocks() {
        return vertexShaderBlocks;
    }

    public Set<String> getFragmentShaderBlocks() {
        return fragmentShaderBlocks;
    }

    public Map<TypedShaderVariable, UniformStore> getUniformStoresByVariable() {
        return uniformStoresByVariable;
    }

    public void joinWith(ShaderComponents shaderComponents) {
        this.vertexIns.addAll(shaderComponents.getVertexIns());
        this.uniforms.addAll(shaderComponents.getUniforms());
        this.vertexOuts.addAll(shaderComponents.getVertexOuts());
        this.vertexShaderBlocks.addAll(shaderComponents.getVertexShaderBlocks());
        this.fragmentShaderBlocks.addAll(shaderComponents.getFragmentShaderBlocks());
    }
}

