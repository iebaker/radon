package xyz.izaak.radon.dispatch;

import xyz.izaak.radon.dispatch.annotation.Subscribe;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ibaker on 29/11/2016.
 */
public class Dispatch {
    private Map<String, List<Subscriber>> subscribersByChannel = new HashMap<>();

    public interface Builder<T> {
        T build();
    }

    @SuppressWarnings("unchecked")
    public <T> T node(Builder<T> wrappedInstanceBuilder) {
        T wrappedInstance = wrappedInstanceBuilder.build();
        for (Method method : wrappedInstance.getClass().getMethods()) {
            for (Subscribe subscription : method.getAnnotationsByType(Subscribe.class)) {
                Subscriber subscriber = new Subscriber(wrappedInstance, method);
                subscribersByChannel.putIfAbsent(subscription.channel(), new LinkedList<>());
                subscribersByChannel.get(subscription.channel()).add(subscriber);
            }
        }
        return (T) NodeWrapper.wrap(wrappedInstance, subscribersByChannel);
    }
}
