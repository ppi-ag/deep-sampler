package org.deepmock.core.model;

import java.lang.reflect.Method;

public class JoinPoint {

    private Class<?> target;

    private Method method;

    public JoinPoint(Class<?> target, Method method) {
        this.target = target;
        this.method = method;
    }

    public Class<?> getTarget() {
        return target;
    }

    public void setTarget(Class<?> target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
