package org.deepsampler.core.model;

@FunctionalInterface
public interface ParameterMatcher<T> {
    boolean matches(T parameter);
}
