package xyz.izaak.radon.external.xml;

import xyz.izaak.radon.external.xml.annotation.XmlChild;
import xyz.izaak.radon.external.xml.annotation.XmlElement;
import xyz.izaak.radon.external.xml.annotation.XmlParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by ibaker on 11/01/2017.
 */
public abstract class XmlElementMapper<T> {

    public abstract String getNamespace();
    public abstract String getElement();
    public abstract void handleParameters(Map<String, String> rawParameters);
    public abstract void handleChild(Object child, String target);
    public abstract T get();

    public static <R> XmlElementMapper<R> fromAnnotatedClass(Class<R> outputClass) {
        return new AnnotatedClassXmlElementMapper<>(outputClass);
    }

    @SuppressWarnings("unchecked")
    private static class AnnotatedClassXmlElementMapper<K> extends XmlElementMapper<K> {
        private XmlElement xmlElementAnnotation;
        private Object[] constructorArguments;
        private Annotation[] constructorArgumentAnnotations;
        private String[] constructorArgumentTargets;
        private String[] constructorArgumentParamNames;
        private Class<?>[] constructorArgumentTypes;
        private Constructor<K> validConstructor;
        private Map<String, Method> childAcceptersByTarget = new HashMap<>();
        private Map<Class<?>, Method> childAcceptersByClass = new HashMap<>();
        private Map<Method, List<Object>> methodsToParameters = new HashMap<>();

        AnnotatedClassXmlElementMapper(Class<K> outputClass) {
            xmlElementAnnotation = outputClass.getAnnotation(XmlElement.class);
            if (xmlElementAnnotation == null)
                throw new IllegalArgumentException("That class doesn't have an xml element annotation");
            initializeFrom(outputClass);
        }

        @Override
        public String getNamespace() {
            return xmlElementAnnotation.namespace();
        }

        @Override
        public String getElement() {
            return xmlElementAnnotation.element();
        }

        @Override
        public void handleParameters(Map<String, String> rawParameters) {
            for (int i = 0; i < constructorArgumentParamNames.length; i++) {
                String paramName = constructorArgumentParamNames[i];
                if (!rawParameters.containsKey(paramName) && paramName != null) {
                    throw new IllegalStateException("xml element missing a param");
                }
                String rawParameter = rawParameters.get(paramName);
                constructorArguments[i] = parseRawParameterAs(constructorArgumentTypes[i], rawParameter);
            }
        }

        @Override
        public void handleChild(Object child, String target) {
            for (int i = 0; i < constructorArguments.length; i++) {
                Optional<String> optionalTarget = Optional.ofNullable(constructorArgumentTargets[i]);
                if (optionalTarget.isPresent() && optionalTarget.get().equals(target)) {
                    constructorArguments[i] = child;
                    return;
                }

                Class<?> currentType = constructorArgumentTypes[i];
                if (currentType.isAssignableFrom(child.getClass())) {
                    constructorArguments[i] = child;
                    return;
                }
            }

            if (childAcceptersByTarget.containsKey(target)) {
                Method method = childAcceptersByTarget.get(target);
                methodsToParameters.putIfAbsent(method, new ArrayList<>());
                methodsToParameters.get(method).add(child);
                return;
            }

            if (childAcceptersByClass.containsKey(child.getClass())) {
                Method method = childAcceptersByClass.get(target);
                methodsToParameters.putIfAbsent(method, new ArrayList<>());
                methodsToParameters.get(method).add(child);
            }
        }

        @Override
        public K get() {
            K instance;
            try {
                instance = validConstructor.newInstance(constructorArguments);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new IllegalStateException("couldn't initialize from found constructor!");
            }

            for (Map.Entry<Method, List<Object>> entry : methodsToParameters.entrySet()) {
                Method method = entry.getKey();
                for (Object parameter : entry.getValue()) {
                    try {
                        method.invoke(instance, parameter);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        throw new IllegalStateException("could not invoke child accepting method with supplied parameter");
                    }
                }
            }

            return instance;
        }

        private void initializeFrom(Class<K> outputClass) {
            constructorLoop:
            for (Constructor<K> constructor : (Constructor<K>[]) outputClass.getConstructors()) {
                Parameter[] parameters = constructor.getParameters();
                constructorArguments = new Object[parameters.length];
                constructorArgumentAnnotations = new Annotation[parameters.length];
                constructorArgumentTargets = new String[parameters.length];
                constructorArgumentParamNames = new String[parameters.length];
                constructorArgumentTypes = new Class<?>[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Parameter current = parameters[i];
                    constructorArgumentTypes[i] = current.getType();

                    Optional<XmlChild> xmlChildOptional = Optional.ofNullable(current.getAnnotation(XmlChild.class));
                    if (xmlChildOptional.isPresent()) {
                        XmlChild annotation = xmlChildOptional.get();
                        constructorArgumentAnnotations[i] = annotation;
                        constructorArgumentTargets[i] = annotation.value().length() > 0 ? annotation.value() : null;
                        constructorArgumentParamNames[i] = null;
                        continue;
                    }

                    Optional<XmlParam> xmlParamOptional = Optional.ofNullable(current.getAnnotation(XmlParam.class));
                    if (xmlParamOptional.isPresent()) {
                        XmlParam annotation = xmlParamOptional.get();
                        constructorArgumentAnnotations[i] = annotation;
                        constructorArgumentTargets[i] = null;
                        constructorArgumentParamNames[i] = annotation.value();
                        continue;
                    }
                    continue constructorLoop;
                }
                validConstructor = constructor;
                break;
            }

            if (validConstructor == null) throw new IllegalStateException("class didn't have a valid constructor");

            for (Method method : outputClass.getMethods()) {
                Optional<XmlChild> xmlChildOptional = Optional.ofNullable(method.getAnnotation(XmlChild.class));
                if (xmlChildOptional.isPresent()) {
                    XmlChild annotation = xmlChildOptional.get();
                    if (annotation.value().length() > 0) {
                        childAcceptersByTarget.put(annotation.value(), method);
                    } else {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != 1)
                            throw new IllegalStateException("child accepting method should accept exactly 1 parameter");
                        childAcceptersByClass.put(parameterTypes[0], method);
                    }
                }
            }

        }

        private Object parseRawParameterAs(Class<?> type, String rawParameter) {
            if (type.isAssignableFrom(String.class)) return rawParameter;
            if (type.isAssignableFrom(Float.TYPE)) return Float.parseFloat(rawParameter);
            if (type.isAssignableFrom(Integer.TYPE)) return Integer.parseInt(rawParameter);
            if (type.isAssignableFrom(Long.TYPE)) return Long.parseLong(rawParameter);
            if (type.isAssignableFrom(Short.TYPE)) return Short.parseShort(rawParameter);
            if (type.isAssignableFrom(Boolean.TYPE)) return Boolean.parseBoolean(rawParameter);
            if (type.isAssignableFrom(Character.TYPE)) return rawParameter.charAt(0);
            if (type.isAssignableFrom(Double.TYPE)) return Double.parseDouble(rawParameter);
            if (type.isAssignableFrom(Byte.TYPE)) return Byte.parseByte(rawParameter);
            throw new IllegalStateException("xml param was not primitive or string");
        }
    }
}
