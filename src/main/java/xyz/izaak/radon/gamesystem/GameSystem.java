package xyz.izaak.radon.gamesystem;

import org.joml.Vector2f;

/**
 * A {@link GameSystem} is the fundamental framework of functionality in a Radon game. A Radon Game comprises a set of
 * GameSystem objects whose methods it calls in response to initialization, ticks, and input events.
 * By default, all methods are no-ops, which allows each GameSystem to only implement the specific
 * callbacks it needs.
 */
public interface GameSystem {

    /**
     * Called after the OpenGL context is initialized, but before the game loop begins
     */
    default void initialize() { }

    /**
     * Called each frame
     * @param seconds how much time in seconds has elapsed since the previous frame
     */
    default void update(float seconds) { }

    /**
     * Called once when a key is pressed down
     * @param key the GLFW key code of the key which was pressed down
     */
    default void onKeyDown(int key) { }

    /**
     * Called repeatedly while a key is held down
     * @param key the GLFW key code of the key which is held down
     */
    default void onKeyHeld(int key) { }

    /**
     * Called repeatedly while a key is held down, but not the first time
     * @param key the GLFW key code of the key which is being repeated
     */
    default void onKeyRepeat(int key) { }

    /**
     * Called once when a key is released
     * @param key the GLFW key code of the key which was released
     */
    default void onKeyUp(int key) { }

    /**
     * Called each time the mouse moves
     * @param delta the distance, in pixels, that the mouse has moved since the previous frame
     */
    default void onMouseMove(Vector2f delta) { }

    /**
     * Called once when a mouse button is pressed
     * @param position the location of the mouse, in pixels, from the corner of the window
     * @param button the GLFW mouse button code for the button which was pressed
     */
    default void onMouseDown(Vector2f position, int button) { }

    /**
     * Called once when a mouse button is released
     * @param position the location of the mouse, in pixels, from the corner of the window
     * @param button the GLFW mouse button code for the button which was released
     */
    default void onMouseUp(Vector2f position, int button) { }

    /**
     * Called when the mouse scroll wheel (or trackpad) is used
     * @param x the vertical amount scrolled
     * @param y the horizontal amount scrolled
     */
    default void onScroll(float x, float y) { }

    /**
     * Called when the application window is resized
     * @param newSize the new size, in pixels, of the application window
     */
    default void onResize(Vector2f newSize) { }
}
