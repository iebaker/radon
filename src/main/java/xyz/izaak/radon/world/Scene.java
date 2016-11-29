package xyz.izaak.radon.world;

import org.joml.Vector3f;
import xyz.izaak.radon.world.arg.SceneConstructionArg;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ibaker on 22/08/2016.
 */
public class Scene {
    private Set<Entity> entities = new HashSet<>();
    private Vector3f gravity;

    public Scene(SceneConstructionArg arg) {
        this.gravity = arg.getGravity();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public Vector3f getGravity() {
        return gravity;
    }

    public Set<Entity> getEntities() {
        return entities;
    }
}
