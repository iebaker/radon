package xyz.izaak.radon.world;

import org.joml.Vector3f;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A collection of objects which are grouped to be rendered and physically interact, as well as
 * some global properties such as gravity. A Scene does not necessarily imply a specific way in which it will be
 * rendered or physically simulated.
 */
@ProvidesShaderComponents
public class Scene {

    /**
     * The maximum number of directional lights a Scene can contain
     */
    public static final int MAX_DIRECTIONAL_LIGHTS = 4;

    /**
     * The maximum number of point lights a Scene can contain
     */
    public static final int MAX_POINT_LIGHTS = 256;

    private Set<Entity> entities = new HashSet<>();
    private Set<DirectionalLight> directionalLights = new HashSet<>();
    private Set<PointLight> pointLights = new HashSet<>();
    private Vector3f gravity;

    /**
     * @return a Scene.Builder object
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder class for {@link Scene} objects
     */
    public static class Builder {
        private Vector3f gravity = new Vector3f(0.0f, -10.0f, 0.0f);

        /**
         * Sets the direction of gravity for this Scene.
         *
         * @param gravity a 3-vector representing the direction of gravity in this Scene.
         *                By default, it's value is (0, -10, 0).
         * @return this builder object
         */
        public Builder gravity(Vector3f gravity) {
            this.gravity.set(gravity);
            return this;
        }

        /**
         * Constructs a Scene with the properties as set in this builder.
         * @return this builder object
         */
        public Scene build() {
            return new Scene(gravity);
        }
    }

    /**
     * Constructs a Scene
     * @param gravity a 3-vector representing the direction of gravity in this Scene
     */
    public Scene(Vector3f gravity) {
        this.gravity = gravity;
    }

    /**
     * Adds an {@link Entity} object to the Scene.
     * @param entity the Entity to be added
     */
    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * Adds a {@link PointLight} object to the Scene.
     * @param pointLight the PointLight to be added
     */
    public void addPointLight(PointLight pointLight) {
        pointLights.add(pointLight);
    }

    /**
     * Adds a {@link DirectionalLight} object to the Scene
     * @param directionalLight the DirectionalLight to be added
     */
    public void addDirectionalLight(DirectionalLight directionalLight) {
        directionalLights.add(directionalLight);
    }

    /**
     * @return a 3-vector representing the direction of gravity in this Scene
     */
    public Vector3f getGravity() {
        return gravity;
    }

    /**
     * @return a Set view of the Entity objects in this Scene
     */
    public Set<Entity> getEntities() {
        return entities;
    }

    /**
     * @return a Set view of the PointLight objects in this Scene
     */
    public Set<PointLight> getPointLights() {
        return pointLights;
    }

    /**
     * @return a Set view of the DirectionalLight objects in this Scene
     */
    public Set<DirectionalLight> getDirectionalLights() {
        return directionalLights;
    }

    /**
     * @return the number of DirectionalLight objects in this Scene
     */
    @ShaderUniform(identifier = Identifiers.DIRECTIONAL_LIGHT_COUNT)
    public int getNumDirectionalLights() {
        return directionalLights.size();
    }

    /**
     * @return the number of DirectionalLight objects in this Scene
     */
    @ShaderUniform(identifier = Identifiers.POINT_LIGHT_COUNT)
    public int getNumPointLights() {
        return pointLights.size();
    }

    /**
     * Gets directions of directional lights, in the same order as their intensities are returned from
     * {@link #getDirectionalLightIntensities()}
     *
     * @return a List of Vector3f representing the direction each DirectionalLight in the Scene is facing
     */
    @ShaderUniform(length = MAX_DIRECTIONAL_LIGHTS, identifier = Identifiers.DIRECTIONAL_LIGHT_DIRECTIONS)
    public List<Vector3f> getDirectionalLightDirections() {
        return directionalLights.stream().map(DirectionalLight::getDirection).collect(Collectors.toList());
    }

    /**
     * Gets intensities of directional lights, in the same order as their directions are returned from
     * {@link #getDirectionalLightDirections()}
     *
     * @return a List of Vector3f representing the intensity (R, G, B) of each DirectionalLight in the Scene
     */
    @ShaderUniform(length = MAX_DIRECTIONAL_LIGHTS, identifier = Identifiers.DIRECTIONAL_LIGHT_INTENSITIES)
    public List<Vector3f> getDirectionalLightIntensities() {
        return directionalLights.stream().map(DirectionalLight::getIntensity).collect(Collectors.toList());
    }

    /**
     * Gets positions of point lights, in the same order as their intensities are returned from
     * {@link #getPointLightIntensities()}
     *
     * @return a List of Vector3f representing the position of each PointLight in the Scene
     */
    @ShaderUniform(length = MAX_POINT_LIGHTS, identifier = Identifiers.POINT_LIGHT_POSITIONS)
    public List<Vector3f> getPointLightPositions() {
        return pointLights.stream().map(PointLight::getPosition).collect(Collectors.toList());
    }

    /**
     * Gets intensities of point lights, in the same order as their positions are returned from
     * {@link #getPointLightPositions()}
     *
     * @return a List of Vector3f representing the intensity (R,G,B) of each PointLight in the Scene
     */
    @ShaderUniform(length = MAX_POINT_LIGHTS, identifier = Identifiers.POINT_LIGHT_INTENSITIES)
    public List<Vector3f> getPointLightIntensities() {
        return pointLights.stream().map(PointLight::getIntensity).collect(Collectors.toList());
    }
}
