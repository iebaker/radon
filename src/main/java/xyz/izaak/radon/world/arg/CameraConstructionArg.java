package xyz.izaak.radon.world.arg;

import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.shading.Shader;

/**
 * Created by ibaker on 28/11/2016.
 */
public class CameraConstructionArg {
    private float nearPlane;
    private float farPlane;
    private float aspectRatio;
    private float fov;
    private Vector3f eye = new Vector3f();
    private Vector3f look = new Vector3f();
    private Vector3f up = new Vector3f();

    public static class Builder {
        private float nearPlane = 0.1f;
        private float farPlane = 1000.0f;
        private float aspectRatio = 1.3f;
        private float fov = Points.piOver(2);
        private Vector3f eye = Points.copyOf(Points.__z);
        private Vector3f look = Points.copyOf(Points.__Z);
        private Vector3f up = Points.copyOf(Points._Y_);

        public Builder nearPlane(float nearPlane) {
            this.nearPlane = nearPlane;
            return this;
        }

        public Builder farPlane(float farPlane) {
            this.farPlane = farPlane;
            return this;
        }

        public Builder aspectRatio(float aspectRatio) {
            this.aspectRatio = aspectRatio;
            return this;
        }

        public Builder fov(float fov) {
            this.fov = fov;
            return this;
        }

        public Builder eye(Vector3f eye) {
            this.eye.set(eye);
            return this;
        }

        public Builder look(Vector3f look) {
            this.look.set(look);
            return this;
        }

        public Builder up(Vector3f up) {
            this.up.set(up);
            return this;
        }

        public CameraConstructionArg build() {
            return new CameraConstructionArg(nearPlane, farPlane, aspectRatio, fov, eye, look, up);
        }
    }

    public CameraConstructionArg(
            float nearPlane,
            float farPlane,
            float aspectRatio,
            float fov,
            Vector3f eye,
            Vector3f look,
            Vector3f up) {
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        this.aspectRatio = aspectRatio;
        this.fov = fov;
        this.eye.set(eye);
        this.look.set(look);
        this.up.set(up);
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getFov() {
        return fov;
    }

    public Vector3f getEye() {
        return eye;
    }

    public Vector3f getLook() {
        return look;
    }

    public Vector3f getUp() {
        return up;
    }
}
