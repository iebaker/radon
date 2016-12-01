package xyz.izaak.radon.world;

import com.bulletphysics.collision.shapes.CollisionShape;
import org.joml.Matrix4f;
import xyz.izaak.radon.math.MatrixTransformable;
import xyz.izaak.radon.primitive.Primitive;
import xyz.izaak.radon.shading.Identifiers;
import xyz.izaak.radon.shading.annotation.ProvidesShaderComponents;
import xyz.izaak.radon.shading.annotation.ShaderUniform;
import xyz.izaak.radon.world.arg.EntityConstructionArg;

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

    public Entity(EntityConstructionArg arg) {
        this.mass = arg.getMass();
        this.restitution = arg.getRestitution();
        this.friction = arg.getFriction();
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
