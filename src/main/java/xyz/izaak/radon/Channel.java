package xyz.izaak.radon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ibaker on 22/12/2016.
 */
public class Channel<T> {
    public static String CURRENT_CAMERA = "rn.channel.CurrentCamera";
    public static String CURRENT_SCENE = "rn.channel.CurrentScene";

    private static Map<String, Channel> channels = new HashMap<>();
    private static boolean paused = true;

    public static void stopAll() {
        paused = true;
    }

    public static void flowAll() {
        paused = false;
        channels.values().forEach(Channel::onFlow);
    }

    @SuppressWarnings("unchecked")
    public static <K> Channel<K> request(Class<K> messageClass, String name) {
        if (!channels.containsKey(name)) {
            channels.put(name, new Channel<K>());
        }
        return (Channel<K>) channels.get(name);
    }

    private List<Subscription<T>> subscriptions = new ArrayList<>();
    private List<T> queue = new ArrayList<>();
    private String name;

    public interface Subscription<M> {
        void receive(M message);
    }

    public void onFlow() {
        subscriptions.forEach(subscription -> queue.forEach(subscription::receive));
        queue.clear();
    }

    public void publish(T message) {
        if (paused) {
            queue.add(message);
        } else {
            int subscriptionCount = subscriptions.size();
            for (int i = 0; i < subscriptionCount; i++) {
                subscriptions.get(i).receive(message);
            }
        }
    }

    public void subscribe(Subscription<T> subscription) {
        subscriptions.add(subscription);
    }
}
