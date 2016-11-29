package xyz.izaak.radon.dispatch;

import xyz.izaak.radon.dispatch.annotation.Publish;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ibaker on 29/11/2016.
 */
public class NodeWrapper implements InvocationHandler {
    private final Object wrapped;
    private Map<String, List<Subscriber>> subscribersByChannel = new HashMap<>();

    public static Object wrap(Object wrapped, Map<String, List<Subscriber>> subscribersByChannel) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[] { Object.class };
        NodeWrapper wrapper = new NodeWrapper(wrapped, subscribersByChannel);
        return Proxy.newProxyInstance(classLoader, classes, wrapper);
    }

    public NodeWrapper(Object wrapped, Map<String, List<Subscriber>> subscribersByChannel) {
        this.wrapped = wrapped;
        this.subscribersByChannel = subscribersByChannel;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(wrapped, args);
        for (Publish publisher : method.getAnnotationsByType(Publish.class)) {
            for (Subscriber subscriber : subscribersByChannel.get(publisher.channel())) {
                subscriber.getMethod().invoke(subscriber.getObject(), result);
            }
        }
        return result;
    }
}
