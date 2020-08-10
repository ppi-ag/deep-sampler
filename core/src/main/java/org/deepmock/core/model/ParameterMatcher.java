package org.deepmock.core.model;

import java.io.Serializable;

public interface ParameterMatcher extends Serializable {
    boolean matches(Object parameter);
}
