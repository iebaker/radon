package xyz.izaak.radon.gamesystem;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.world.Camera;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

/**
 * Created by ibaker on 03/12/2016.
 */
public class FlyingCameraSystem implements GameSystem {
    private Camera camera;
    private Vector3f worldVertical;
    private Matrix4f modifier;
    private Vector3f cameraForward;
    private Vector3f cameraLeft;
    private Vector3f cameraVelocity;
    private Vector3f cameraVelocityDelta;
    private Vector3f cameraTargetVelocity;
    private float topSpeed;
    private float accelerationFactor;
    private float rotationFactor;

    public FlyingCameraSystem(Camera camera, Vector3f worldVertical) {
        this(camera, worldVertical, 0.1f, 0.5f, 1 / 500.0f);
    }

    public FlyingCameraSystem(
            Camera camera,
            Vector3f worldVertical,
            float topSpeed,
            float accelerationFactor,
            float rotationFactor) {

        this.camera = camera;
        this.worldVertical = worldVertical;
        this.topSpeed = topSpeed;
        this.accelerationFactor = accelerationFactor;
        this.rotationFactor = rotationFactor;

        this.modifier = new Matrix4f();
        this.cameraForward = new Vector3f();
        Points.projectPerpendicular(camera.getLook(), worldVertical, cameraForward);
        this.cameraLeft = new Vector3f(cameraForward);
        modifier.rotation(Points.piOver(2), worldVertical).transformDirection(cameraLeft);

        this.cameraVelocity = new Vector3f();
        this.cameraVelocityDelta = new Vector3f();
        this.cameraTargetVelocity = new Vector3f();
    }

    @Override
    public void onMouseMove(Vector2f delta) {
        float rotationAmountX = -delta.x * rotationFactor;
        float rotationAmountY = delta.y * rotationFactor;

        modifier.rotation(rotationAmountX, worldVertical);
        modifier.transformDirection(cameraForward);
        modifier.transformDirection(cameraLeft);
        camera.rotate(rotationAmountX, worldVertical);

        camera.rotate(rotationAmountY, cameraLeft);
    }

    @Override
    public void onKeyHeld(int key) {
        switch (key) {
            case GLFW_KEY_W:
                cameraTargetVelocity.set(cameraForward).mul(topSpeed);
                break;
            case GLFW_KEY_S:
                cameraTargetVelocity.set(cameraForward).negate().mul(topSpeed);
                break;
            case GLFW_KEY_A:
                cameraTargetVelocity.set(cameraLeft).mul(topSpeed);
                break;
            case GLFW_KEY_D:
                cameraTargetVelocity.set(cameraLeft).negate().mul(topSpeed);
                break;
            case GLFW_KEY_SPACE:
                cameraTargetVelocity.set(worldVertical).mul(topSpeed);
                break;
            case GLFW_KEY_LEFT_SHIFT:
                cameraTargetVelocity.set(worldVertical).negate().mul(topSpeed);
                break;
        }
    }

    @Override
    public void onKeyUp(int key) {
        cameraTargetVelocity.zero();
    }

    @Override
    public void update(float seconds) {
        cameraVelocityDelta.set(cameraTargetVelocity).sub(cameraVelocity).mul(accelerationFactor);
        cameraVelocity.add(cameraVelocityDelta);
        camera.translate(cameraVelocity);
    }
}
