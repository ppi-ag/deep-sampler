package org.deepsampler.core.model;

import java.io.Serializable;

public interface ParameterMatcher<T> extends Serializable {
    boolean matches(T parameter);
}
