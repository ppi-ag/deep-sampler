package org.deepsampler.core.model;

public interface ParameterMatcher<T> {
    boolean matches(T parameter);
}
