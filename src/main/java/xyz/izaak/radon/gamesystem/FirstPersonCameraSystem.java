package xyz.izaak.radon.gamesystem;

import org.joml.Vector2f;
import org.joml.Vector3f;
import xyz.izaak.radon.math.Points;
import xyz.izaak.radon.world.Camera;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

/**
 * Created by ibaker on 03/12/2016.
 */
public class FirstPersonCameraSystem implements GameSystem {
    private Camera camera;
    private Vector3f scratch;

    public FirstPersonCameraSystem(Camera camera) {
        this.camera = camera;
        this.scratch = new Vector3f();
    }

    @Override
    public void onMouseMove(Vector2f delta) {
        camera.rotate(delta.x / 150, Points.__z);
        camera.rotate(delta.y / 150, scratch.set(camera.getUp()).cross(camera.getLook()));
    }

    @Override
    public void onKeyHeld(int key) {
        switch (key) {
            case GLFW_KEY_UP:
            case GLFW_KEY_W:
                camera.translate(camera.getLook());
                break;
            case GLFW_KEY_DOWN:
            case GLFW_KEY_S:
                camera.translate(scratch.set(camera.getLook()).negate());
                break;
            case GLFW_KEY_LEFT:
            case GLFW_KEY_A:
                camera.translate(scratch.set(camera.getUp()).cross(camera.getLook()));
                break;
            case GLFW_KEY_RIGHT:
            case GLFW_KEY_D:
                camera.translate(scratch.set(camera.getUp()).cross(camera.getLook()).negate());
                break;
        }
    }
}
