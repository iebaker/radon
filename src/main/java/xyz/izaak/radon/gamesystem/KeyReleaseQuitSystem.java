package xyz.izaak.radon.gamesystem;

import org.lwjgl.glfw.GLFW;

import static org.lwjgl.opengl.GL11.GL_TRUE;

/**
 * Created by ibaker on 17/08/2016.
 */
public class KeyReleaseQuitSystem implements GameSystem {
    private int trigger;
    private long window;

    public KeyReleaseQuitSystem(long window, int trigger) {
        this.trigger = trigger;
        this.window = window;
    }

    @Override
    public void onKeyUp(int key) {
        if (key == trigger) {
            GLFW.glfwSetWindowShouldClose(window, GL_TRUE);
        }
    }
}
