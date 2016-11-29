package xyz.izaak.radon.world.arg;

import org.joml.Vector3f;

/**
 * Created by ibaker on 28/11/2016.
 */
public class SceneConstructionArg {
    private Vector3f gravity = new Vector3f();

    public static class Builder {
        private Vector3f gravity = new Vector3f(0.0f, -10.0f, 0.0f);

        public Builder gravity(Vector3f gravity) {
            this.gravity.set(gravity);
            return this;
        }

        public SceneConstructionArg build() {
            return new SceneConstructionArg(gravity);
        }
    }

    public SceneConstructionArg(Vector3f gravity) {
        this.gravity.set(gravity);
    }

    public Vector3f getGravity() {
        return gravity;
    }
}
