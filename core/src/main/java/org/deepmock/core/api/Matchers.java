package org.deepmock.core.api;

import org.deepmock.core.model.ParameterMatcher;

import java.util.Objects;

public class Matchers {

    public static ParameterMatcher any() {
        return parameter -> true;
    }

    public static ParameterMatcher specific(Object expectedParameter) {
        return actualParameter -> Objects.equals(actualParameter, expectedParameter);
    }
}
