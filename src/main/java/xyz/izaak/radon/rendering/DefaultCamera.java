package xyz.izaak.radon.rendering;

import java.util.Collection;

/**
 * Created by ibaker on 17/08/2016.
 */
public class DefaultCamera extends Camera implements ShaderVariableProvider {

    private static ShaderVariables shaderVariables = new ShaderVariables();
    private static final String VIEW = "view";
    private static final String PROJECTION = "projection";
    private static final String CAMERA_EYE = "cameraEye";

    static {
        shaderVariables.addUniform(ShaderVariableType.MAT4, DefaultCamera.VIEW);
        shaderVariables.addUniform(ShaderVariableType.MAT4, DefaultCamera.PROJECTION);
        shaderVariables.addUniform(ShaderVariableType.VEC3, DefaultCamera.CAMERA_EYE);
    }

    public static ShaderVariables provideShaderVariables() {
        return shaderVariables;
    }

    @Override
    public <R extends Renderable> void capture(Collection<R> renderables) {
        for (R renderable : renderables) {
            // render them!
        }
    }
}
