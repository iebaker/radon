package xyz.izaak.radon.world;

import org.joml.Vector3f;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.UniformProvider;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A collection of objects which are grouped to be rendered and physically interact, as well as
 * some global properties such as gravity. A Scene does not necessarily imply a specific way in which it will be
 * rendered or physically simulated.
 */
@ProvidesShaderComponents
public class Scene implements UniformProvider {

    public static final int MAX_DIRECTIONAL_LIGHTS = 4;
    public static final int MAX_POINT_LIGHTS = 256;

    private Set<Entity> entities = new HashSet<>();
    private Set<DirectionalLight> directionalLights = new HashSet<>();
    private Set<PointLight> pointLights = new HashSet<>();
    private Map<UUID, Portal> portalsById = new HashMap<>();
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

    public void addPortal(Portal portal) {
        portal.setParentScene(this);
        portalsById.put(portal.getUuid(), portal);
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

    public Collection<Portal> getPortals() {
        return portalsById.values();
    }

    @ShaderUniform(identifier = Identifiers.DIRECTIONAL_LIGHT_COUNT)
    public int getNumDirectionalLights() {
        return directionalLights.size();
    }

    @ShaderUniform(identifier = Identifiers.POINT_LIGHT_COUNT)
    public int getNumPointLights() {
        return pointLights.size();
    }

    @ShaderUniform(length = MAX_DIRECTIONAL_LIGHTS, identifier = Identifiers.DIRECTIONAL_LIGHT_DIRECTIONS)
    public List<Vector3f> getDirectionalLightDirections() {
        return directionalLights.stream().map(DirectionalLight::getDirection).collect(Collectors.toList());
    }

    @ShaderUniform(length = MAX_DIRECTIONAL_LIGHTS, identifier = Identifiers.DIRECTIONAL_LIGHT_INTENSITIES)
    public List<Vector3f> getDirectionalLightIntensities() {
        return directionalLights.stream().map(DirectionalLight::getIntensity).collect(Collectors.toList());
    }

    @ShaderUniform(length = MAX_POINT_LIGHTS, identifier = Identifiers.POINT_LIGHT_POSITIONS)
    public List<Vector3f> getPointLightPositions() {
        return pointLights.stream().map(PointLight::getPosition).collect(Collectors.toList());
    }

    @ShaderUniform(length = MAX_POINT_LIGHTS, identifier = Identifiers.POINT_LIGHT_INTENSITIES)
    public List<Vector3f> getPointLightIntensities() {
        return pointLights.stream().map(PointLight::getIntensity).collect(Collectors.toList());
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.DIRECTIONAL_LIGHT_COUNT, getNumDirectionalLights());
        shader.setUniform(Identifiers.POINT_LIGHT_COUNT, getNumPointLights());
        shader.setUniform(Identifiers.DIRECTIONAL_LIGHT_DIRECTIONS, getDirectionalLightDirections());
        shader.setUniform(Identifiers.DIRECTIONAL_LIGHT_INTENSITIES, getDirectionalLightIntensities());
        shader.setUniform(Identifiers.POINT_LIGHT_POSITIONS, getPointLightPositions());
        shader.setUniform(Identifiers.POINT_LIGHT_INTENSITIES, getPointLightIntensities());
    }
}
