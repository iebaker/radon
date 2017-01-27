package xyz.izaak.radon.gamesystem;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.izaak.radon.Channel;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.scene.Camera;
import xyz.izaak.radon.scene.Portal;
import xyz.izaak.radon.scene.Scene;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

/**
 * Created by ibaker on 03/12/2016.
 */
public class FlyingCameraSystem implements GameSystem {
    private Camera camera;
    private Scene scene;

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

    private Channel<Scene> currentSceneChannel;

    public FlyingCameraSystem(Vector3f worldVertical) {
        this(worldVertical, 0.2f, 0.1f, 1 / 500.0f);
    }

    public FlyingCameraSystem(
            Vector3f worldVertical,
            float topSpeed,
            float accelerationFactor,
            float rotationFactor) {

        this.worldVertical = worldVertical;
        this.topSpeed = topSpeed;
        this.accelerationFactor = accelerationFactor;
        this.rotationFactor = rotationFactor;
        this.modifier = new Matrix4f();
        this.cameraForward = new Vector3f();
        this.cameraVelocity = new Vector3f();
        this.cameraVelocityDelta = new Vector3f();
        this.cameraTargetVelocity = new Vector3f();
    }

    @Override
    public void initialize() {
        currentSceneChannel = Channel.request(Scene.class, Channel.CURRENT_SCENE);
        currentSceneChannel.subscribe(scene -> this.scene = scene);

        Channel.request(Camera.class, Channel.CURRENT_CAMERA).subscribe(camera -> {
            Points.projectPerpendicular(camera.getLook(), worldVertical, cameraForward);
            this.cameraLeft = new Vector3f(cameraForward);
            modifier.rotation(Points.piOver(2), worldVertical).transformDirection(cameraLeft);
            this.camera = camera;
        });
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

        Portal crossed = null;
        int portalCount = scene.getPortals().size();
        for (int i = 0; i < portalCount; i++) {
            Portal portal = scene.getPortals().get(i);
            if (portal.crossedBy(camera.getEye(), cameraVelocity)) {
                crossed = portal;
                break;
            }
        }

        camera.translate(cameraVelocity);
        if (crossed != null && crossed.getChildPortal() != null) {
            camera.shiftPerspective(crossed);
            crossed.transformDirection(cameraForward);
            crossed.transformDirection(cameraLeft);
            crossed.transformDirection(cameraVelocity);
            currentSceneChannel.publish(crossed.getChildPortal().getParentScene());
        }
    }
}
