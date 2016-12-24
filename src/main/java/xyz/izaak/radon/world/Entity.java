package xyz.izaak.radon.world;

import com.bulletphysics.collision.shapes.CollisionShape;
import org.joml.Matrix4f;
import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.mesh.Mesh;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.Shader;
import xyz.izaak.radon.shading.UniformProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Any object within a Scene. Most Entity objects will have a set of {@link Mesh} objects
 * which comprise its physical appearance, as well as a single CollisionShape and some constant values which govern
 * the way this Entity interacts kinematically with other Entities in the scene.
 */
public class Entity extends MatrixTransformable implements UniformProvider {
    private List<Mesh> meshes = new ArrayList<>();
    private CollisionShape collisionShape;
    private float mass;
    private float restitution;
    private float friction;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float mass = 0.0f;
        private float restitution = 0.5f;
        private float friction = 1.5f;

        public Builder mass(float mass) {
            this.mass = mass;
            return this;
        }

        public Builder restitution(float restitution) {
            this.restitution = restitution;
            return this;
        }

        public Builder friction(float friction) {
            this.friction = friction;
            return this;
        }

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

    public void addMeshes(Mesh... meshes) {
        this.meshes.addAll(Arrays.asList(meshes));
    }

    public void setCollisionShape(CollisionShape collisionShape) {
        this.collisionShape = collisionShape;
    }

    public float getMass() {
        return mass;
    }

    public float getRestitution() {
        return restitution;
    }

    public float getFriction() {
        return friction;
    }

    public List<Mesh> getMeshes() {
        return meshes;
    }

    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    public Matrix4f getModel() {
        return super.getModel();
    }

    @Override
    public void setUniformsOn(Shader shader) {
        shader.setUniform(Identifiers.ENTITY_MODEL, getModel());
    }

    @Override
    public void scale(float x, float y, float z) {
        System.out.println("Entity level scaling not supported!");
    }
}
