package xyz.izaak.radon.dispatch;

import java.lang.reflect.Method;

/**
 * Created by ibaker on 29/11/2016.
 */
public class Subscriber {
    private Object object;
    private Method method;

    public Subscriber(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    public Object getObject() {
        return object;
    }

    public Method getMethod() {
        return method;
    }
}
