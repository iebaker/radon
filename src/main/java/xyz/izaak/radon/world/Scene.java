package xyz.izaak.radon.world;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ibaker on 22/08/2016.
 */
public class Scene {
    private Set<Entity> entities = new HashSet<>();

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public Set<Entity> getEntities() {
        return entities;
    }
}
