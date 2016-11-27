package xyz.izaak.radon.gamesystem;

import org.joml.Vector2f;

/**
 * Created by ibaker on 17/08/2016.
 */
public interface GameSystem {
    default void initialize() { }
    default void update(float seconds) { }
    default void onKeyDown(int key) { }
    default void onKeyHeld(int key) { }
    default void onKeyRepeat(int key) { }
    default void onKeyUp(int key) { }
    default void onMouseMove(Vector2f delta) { }
    default void onMouseDown(Vector2f position, int button) { }
    default void onMouseUp(Vector2f position, int button) { }
    default void onScroll(float x, float y) { }
    default void onResize(Vector2f newSize) { }
}
