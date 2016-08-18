package xyz.izaak.radon.gamesystem;

/**
 * Created by ibaker on 17/08/2016.
 */
public class KeyReleaseQuitSystem extends DefaultGameSystem {
    private int trigger;

    public KeyReleaseQuitSystem(int trigger) {
        this.trigger = trigger;
    }

    @Override
    public void onKeyUp(int key) {
        if (key == trigger) {
            System.exit(0);
        }
    }
}
