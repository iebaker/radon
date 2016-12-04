package xyz.izaak.radon.world;

import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ibaker on 22/08/2016.
 */
public class Scene {
    private Set<Entity> entities = new HashSet<>();
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

    public Vector3f getGravity() {
        return gravity;
    }

    public Set<Entity> getEntities() {
        return entities;
    }
}
