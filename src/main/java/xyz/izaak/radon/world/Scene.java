package xyz.izaak.radon.world;

import org.joml.Vector3f;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ProvidesShaderComponents
public class Scene {
    private Set<Entity> entities = new HashSet<>();
    private Set<DirectionalLight> directionalLights = new HashSet<>();
    private Set<PointLight> pointLights = new HashSet<>();
    private Vector3f gravity;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Vector3f gravity = new Vector3f(0.0f, -10.0f, 0.0f);

        public Builder gravity(Vector3f gravity) {
            this.gravity.set(gravity);
            return this;
        }

        public Scene build() {
            return new Scene(gravity);
        }
    }

    public Scene(Vector3f gravity) {
        this.gravity = gravity;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void addPointLight(PointLight pointLight) {
        pointLights.add(pointLight);
    }

    public void addDirectionalLight(DirectionalLight directionalLight) {
        directionalLights.add(directionalLight);
    }

    public Vector3f getGravity() {
        return gravity;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public Set<PointLight> getPointLights() {
        return pointLights;
    }

    public Set<DirectionalLight> getDirectionalLights() {
        return directionalLights;
    }

    @ShaderUniform(length = 256, identifier = Identifiers.DIRECTIONAL_LIGHT_DIRECTIONS)
    public List<Vector3f> getDirectionalLightDirections() {
        return directionalLights.stream().map(DirectionalLight::getDirection).collect(Collectors.toList());
    }

    @ShaderUniform(length = 256, identifier = Identifiers.DIRECTIONAL_LIGHT_INTENSITIES)
    public List<Vector3f> getDirectionalLightIntensities() {
        return directionalLights.stream().map(DirectionalLight::getIntensity).collect(Collectors.toList());
    }

    @ShaderUniform(length = 256, identifier = Identifiers.POINT_LIGHT_POSITIONS)
    public List<Vector3f> getPointLightPositions() {
        return pointLights.stream().map(PointLight::getPosition).collect(Collectors.toList());
    }

    @ShaderUniform(length = 256, identifier = Identifiers.POINT_LIGHT_INTENSITIES)
    public List<Vector3f> getPointLightIntensities() {
        return pointLights.stream().map(PointLight::getIntensity).collect(Collectors.toList());
    }
}
