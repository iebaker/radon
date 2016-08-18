package xyz.izaak.radon.gamesystem;

import org.joml.Vector2f;

/**
 * Created by ibaker on 17/08/2016.
 */
public interface GameSystem {
    void initialize();
    void update(float seconds);
    void onKeyDown(int key);
    void onKeyHeld(int key);
    void onKeyRepeat(int key);
    void onKeyUp(int key);
    void onMouseMove(Vector2f delta);
    void onMouseDown(Vector2f position, int button);
    void onMouseUp(Vector2f position, int button);
    void onScroll(float x, float y);
    void onResize(Vector2f newSize);
}
