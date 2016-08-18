package xyz.izaak.radon.rendering;

import java.util.Set;

/**
 * Created by ibaker on 17/08/2016.
 */
public class ShaderVariables {

    private Set<TypedShaderVariable> fragmentIns;
    private Set<TypedShaderVariable> uniforms;
    private Set<TypedShaderVariable> fragmentOuts;

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

    public void addUniform(ShaderVariableType type, String name) {
        uniforms.add(new TypedShaderVariable(type, name));
    }

    public void addFragmentOut(ShaderVariableType type, String name) {
        fragmentOuts.add(new TypedShaderVariable(type, name));
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

    public void joinWith(ShaderVariables shaderVariables) {
        this.fragmentIns.addAll(shaderVariables.getFragmentIns());
        this.uniforms.addAll(shaderVariables.getUniforms());
        this.fragmentOuts.addAll(shaderVariables.getFragmentOuts());
    }
}

