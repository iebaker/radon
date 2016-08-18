package xyz.izaak.radon.rendering;

import java.util.Collection;

/**
 * Created by ibaker on 17/08/2016.
 */
public class DefaultCamera extends Camera implements ShaderComponentProvider {

    private static ShaderComponents shaderComponents = new ShaderComponents();
    private static final String VIEW = "view";
    private static final String PROJECTION = "projection";
    private static final String CAMERA_EYE = "cameraEye";

    static {
        shaderComponents.addUniform(ShaderVariableType.MAT4, DefaultCamera.VIEW);
        shaderComponents.addUniform(ShaderVariableType.MAT4, DefaultCamera.PROJECTION);
        shaderComponents.addUniform(ShaderVariableType.VEC3, DefaultCamera.CAMERA_EYE);
    }

    public static ShaderComponents provideShaderComponents() {
        return shaderComponents;
    }

    @Override
    public <R extends Renderable> void capture(Collection<R> renderables) {
        for (R renderable : renderables) {
            // render them!
        }
    }
}
