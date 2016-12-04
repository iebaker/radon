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
 * Created by ibaker on 22/08/2016.
 */
@ProvidesShaderComponents
public class Entity extends MatrixTransformable {
    private Set<Primitive> primitives = new HashSet<>();
    private CollisionShape collisionShape;
    private float mass;
    private float restitution;
    private float friction;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float mass = 5.0f;
        private float restitution = 0.5f;
        private float friction = 2.5f;

        public Builder mass(float mass) {
            this.mass = mass;
            return this;
        }

        public Builder restitution(float restitution) {
            this.restitution = restitution;
            return this;
        }

        public Entity build() {
            return new Entity(mass, restitution, friction);
        }
    }

    public Entity(float mass, float restitution, float friction) {
        this.mass = mass;
        this.restitution = restitution;
        this.friction = friction;
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

    public Set<Primitive> getPrimitives() {
        return primitives;
    }

    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    public void addPrimitives(Primitive... primitives) {
        this.primitives.addAll(Arrays.asList(primitives));
    }

    public void setCollisionShape(CollisionShape collisionShape) {
        this.collisionShape = collisionShape;
    }

    @ShaderUniform(identifier = Identifiers.ENTITY_MODEL)
    public Matrix4f getModel() {
        return super.getModel();
    }
}
