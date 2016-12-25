package xyz.izaak.radon.world;

import org.joml.Vector3f;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.UniformProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A collection of objects which are grouped to be rendered and physically interact, as well as
 * some global properties such as gravity. A Scene does not necessarily imply a specific way in which it will be
 * rendered or physically simulated.
 */
public class Scene implements UniformProvider {

    public static final int MAX_DIRECTIONAL_LIGHTS = 4;
    public static final int MAX_POINT_LIGHTS = 256;

    private List<Entity> entities = new ArrayList<>();
    private List<DirectionalLight> directionalLights = new ArrayList<>(MAX_DIRECTIONAL_LIGHTS);
    private List<PointLight> pointLights = new ArrayList<>(MAX_POINT_LIGHTS);

    private List<Vector3f> directionalLightDirections = new ArrayList<>(MAX_DIRECTIONAL_LIGHTS);
    private List<Vector3f> directionalLightIntensities = new ArrayList<>(MAX_DIRECTIONAL_LIGHTS);
    private List<Vector3f> pointLightPositions = new ArrayList<>(MAX_POINT_LIGHTS);
    private List<Vector3f> pointLightIntensities = new ArrayList<>(MAX_POINT_LIGHTS);

    private List<Portal> portals = new ArrayList<>();
    private Map<UUID, Portal> portalsById = new HashMap<>();
    private Vector3f gravity;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Vector3f gravity = new Vector3f(0.0f, -10.0f, 0.0f);
        private String name = UUID.randomUUID().toString();

        public Builder gravity(Vector3f gravity) {
            this.gravity.set(gravity);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Scene build() {
            return new Scene(name, gravity);
        }
    }

    public Scene(String name, Vector3f gravity) {
        this.gravity = gravity;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void addPointLight(PointLight pointLight) {
        pointLights.add(pointLight);
        pointLightPositions.add(pointLight.getPosition());
        pointLightIntensities.add(pointLight.getIntensity());
    }

    public void addDirectionalLight(DirectionalLight directionalLight) {
        directionalLights.add(directionalLight);
        directionalLightDirections.add(directionalLight.getDirection());
        directionalLightIntensities.add(directionalLight.getIntensity());
    }

    public void addPortal(Portal portal) {
        portal.setParentScene(this);
        portalsById.put(portal.getUuid(), portal);
        portals.add(portal);
    }

    public Vector3f getGravity() {
        return gravity;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public List<PointLight> getPointLights() {
        return pointLights;
    }

    public List<DirectionalLight> getDirectionalLights() {
        return directionalLights;
    }

    public List<Portal> getPortals() {
        return portals;
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.DIRECTIONAL_LIGHT_COUNT, directionalLights.size());
        shader.setUniform(Identifiers.POINT_LIGHT_COUNT, pointLights.size());
        shader.setUniform(
                Identifiers.DIRECTIONAL_LIGHT_DIRECTIONS,
                MAX_DIRECTIONAL_LIGHTS,
                directionalLightDirections);
        shader.setUniform(
                Identifiers.DIRECTIONAL_LIGHT_INTENSITIES,
                MAX_DIRECTIONAL_LIGHTS,
                directionalLightIntensities);
        shader.setUniform(
                Identifiers.POINT_LIGHT_POSITIONS,
                MAX_POINT_LIGHTS,
                pointLightPositions);
        shader.setUniform(
                Identifiers.POINT_LIGHT_INTENSITIES,
                MAX_POINT_LIGHTS,
                pointLightIntensities);
    }
}
