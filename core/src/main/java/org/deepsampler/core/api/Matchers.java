package org.deepsampler.core.api;

import org.deepsampler.core.model.ParameterMatcher;
import org.deepsampler.core.model.SampleRepository;

import java.util.Objects;

public class Matchers {

    private Matchers() {
        // This class is not intended to be instantiated, therefore the constructor is private.
    }

    /**
     * Accepts any parameter value.
     * @return a matcher that accepts any parameter value
     */
    @SuppressWarnings("unused")
    public static <T> T any(final Class<T> type) {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return null;
    }

    public static String anyString() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return "";
    }

    public static int anyInt() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return 42;
    }

    public static double anyDouble() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return 42.0;
    }

    public static boolean anyBoolean() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return true;
    }

    public static short anyShort() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return (short) 42;
    }

    public static float anyFloat() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return 42.0f;
    }

    public static byte anyByte() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return (byte) 42;
    }

    public static char anyChar() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return 'c';
    }


    /**
     * Accepts a parameter that equals to expectedParameter. Equality is tested using the .equals()-Method.
     * @param expectedParameter the expected value
     * @return a matcher that accepts a parameter that is equal to expectedParameter. Equality is tested using the .equals()-Method.
     */
    public static <T> T equalTo(final Object expectedParameter) {
        SampleRepository.getInstance().addCurrentParameterMatchers(new EqualsMatcher(expectedParameter));
        return null;
    }

    /**
     * Accepts a parameter-object that is identical to expectedParameter. Equality is tests using the == operator.
     * @param expectedParameter the expected parameter object
     * @return a matcher that accepts a parameter object that is expected to be the same object as expectedParameter.
     */
    public static <T> T sameAs(final Object expectedParameter) {
        SampleRepository.getInstance().addCurrentParameterMatchers(actualParameter -> actualParameter == expectedParameter);
        return null;
    }

    public static class EqualsMatcher implements ParameterMatcher {

        private final Object expectedObject;

        public EqualsMatcher(final Object expectedObject) {
            this.expectedObject = expectedObject;
        }

        @Override
        public boolean matches(final Object parameter) {
            return Objects.equals(expectedObject, parameter);
        }
    }
}
