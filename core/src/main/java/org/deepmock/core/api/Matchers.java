package org.deepmock.core.api;

import org.deepmock.core.model.ParameterMatcher;

import java.util.Objects;

public class Matchers {

    /**
     * Accepts any parameter value.
     * @return a matcher that accepts any parameter value
     */
    public static ParameterMatcher any() {
        return parameter -> true;
    }

    /**
     * Accepts a parameter that equals to expectedParameter. Equality is tested using the .equals()-Method.
     * @param expectedParameter the expected value
     * @return a matcher that accepts a parameter that is equal to expectedParameter. Equality is tested using the .equals()-Method.
     */
    public static ParameterMatcher equalTo(Object expectedParameter) {
        return actualParameter -> Objects.equals(actualParameter, expectedParameter);
    }

    /**
     * Accepts a parameter-object that is identical to expectedParameter. Equality is tests using the == operator.
     * @param expectedParameter the expected parameter object
     * @returna matcher that accepts a parameter object that is expected to be the same object as expectedParameter.
     */
    public static ParameterMatcher same(Object expectedParameter) {
        return actualParameter -> actualParameter == expectedParameter;
    }
}
