package xyz.izaak.radon.world.arg;

/**
 * Created by ibaker on 28/11/2016.
 */
public class EntityConstructionArg {
    private float mass;
    private float restitution;
    private float friction;

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

        public EntityConstructionArg build() {
            return new EntityConstructionArg(mass, restitution, friction);
        }
    }

    public EntityConstructionArg(float mass, float restitution, float friction) {
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
}
