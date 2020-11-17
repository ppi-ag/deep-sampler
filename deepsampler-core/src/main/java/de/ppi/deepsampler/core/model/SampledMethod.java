/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.lang.reflect.Method;

public class SampledMethod {

    private Class<?> target;

    private Method method;

    public SampledMethod(final Class<?> target, final Method method) {
        this.target = target;
        this.method = method;
    }

    public Class<?> getTarget() {
        return target;
    }

    public void setTarget(final Class<?> target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "SampledMethod {" +
                "target=" + target +
                ", method=" + method +
                '}';
    }
}
