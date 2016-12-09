package xyz.izaak.radon.world;

import com.bulletphysics.collision.shapes.CollisionShape;
import org.joml.Matrix4f;
import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.primitive.Primitive;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Any object within a Scene. Most Entity objects will have a set of {@link Primitive} objects
 * which comprise its physical appearance, as well as a single CollisionShape and some constant values which govern
 * the way this Entity interacts kinematically with other Entities in the scene.
 */
@ProvidesShaderComponents
public class Entity extends MatrixTransformable {
    private Set<Primitive> primitives = new HashSet<>();
    private CollisionShape collisionShape;
    private float mass;
    private float restitution;
    private float friction;

    /**
     * @return an Entity.Builder object
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A Builder class for {@link Entity} objects
     */
    public static class Builder {
        private float mass = 0.0f;
        private float restitution = 0.5f;
        private float friction = 2.5f;

        /**
         * Mass is the resistance of an object to acceleration in response to force. It's default value
         * is 0. This does not imply any specific system of units.
         *
         * @param mass the mass to be set.
         * @return this builder
         */
        public Builder mass(float mass) {
            this.mass = mass;
            return this;
        }

        /**
         * The elasticity of this object in collision. Must be a value between 0 and 1. The default value
         * is 0.5.
         * @param restitution the restitution to be set
         * @return this builder
         */
        public Builder restitution(float restitution) {
            this.restitution = restitution;
            return this;
        }

        /**
         * Constructs an Entity object from the parameters in this builder
         * @return an Entity object
         */
        public Entity build() {
            return new Entity(mass, restitution, friction);
        }
    }

    /**
     * Constructs a new entity object
     * @param mass the resistance of this object to acceleration in response to force
     * @param restitution the elasticity of this object in collision (must be between 0 and 1)
     * @param friction the resistance of this object to sliding against other objects
     */
    public Entity(float mass, float restitution, float friction) {
        this.mass = mass;
        this.restitution = restitution;
        this.friction = friction;
    }

    /**
     * Add Primitives to this object, which comprise its visual appearance
     * @param primitives the array of primitives to be added
     */
    public void addPrimitives(Primitive... primitives) {
        this.primitives.addAll(Arrays.asList(primitives));
    }

    /**
     * Add a CollisionShape to this object, so that it can be involved in a JBullet kinematic simulation
     * @param collisionShape the JBullet CollisionShape object which represents this entity in a kinematic simulation
     */
    public void setCollisionShape(CollisionShape collisionShape) {
        this.collisionShape = collisionShape;
    }

    /**
     * @return the resistance of this object to acceleration in response to force
     */
    public float getMass() {
        return mass;
    }

    /**
     * @return elasticity of this object in collision, on a scale from 0 (inelastic) to 1 (elastic)
     */
    public float getRestitution() {
        return restitution;
    }

    /**
     * @return the resistance of this object to sliding against other objects
     */
    public float getFriction() {
        return friction;
    }

    /**
     * @return a Set view of the Primitives which make up this object's visual appearance
     */
    public Set<Primitive> getPrimitives() {
        return primitives;
    }

    /**
     * @return the JBullet CollisionShape which represents this entity in a kinematic simulation
     */
    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    @ShaderUniform(identifier = Identifiers.ENTITY_MODEL)
    public Matrix4f getModel() {
        return super.getModel();
    }
}
