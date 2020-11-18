
/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.model.ParameterMatcher;
import de.ppi.deepsampler.core.model.SampleRepository;

import java.util.Objects;

/**
 * <p>
 * When we are creating a stub with {@link Sample#of(Object)} we would normally call the method we want to stub with the parameters we expect
 * that they will be used in the real invocation later. But there are two problems with this approach:
 * </p>
 * <ul>
 *     <li>We don't always know which parameter will occur in the real invocation</li>
 *     <li>We might want to define a return value for a whole set of parameters</li>
 * </ul>
 * <p>
 * To solve this problems DeepSampler introduces {@link Matchers}. This class contains some factory methods to create common matchers.
 * A typical usage could look like this:
 * </p>
 * <br>
 * <code>
 *      TestBean testBean = Sampler.prepare(TestBean.class);<br>
 *      Sample.of(testBean.echoParam(Matchers.any(String.class))).is("Hello!");
 * </code>
 * <br>
 * <br>
 * <p>
 * With this definition every call of <code>echoParam</code>, regardless of the concrete parameter, will return "Hello!".<br>
 * </p>
 * <p>
 * There are two things you have to care about when using matchers:
 * </p>
 * <ul>
 *     <li>You cant't mix matchers and normal parameters when defining a sample.</li>
 *     <li>The order of method calls when defining a sample matters! So you have to call it within the method invocation you want to stub!</li>
 * </ul>
 * To use your own matcher you have to implement {@link ParameterMatcher} and call {@link Matchers#matcher(ParameterMatcher)} withing the method
 * invocation you want to stub.
 */
public class Matchers {

    private Matchers() {
        // This class is not intended to be instantiated, therefore the constructor is private.
    }

    /**
     * Accepts any parameter value.
     * @return a matcher that accepts any parameter value
     * @param <T> The type of the parameter that will be accepted
     * @param type The {@link Class} of the parameter that will be accepted independently from its concrete value.
     */
    @SuppressWarnings("unused")
    public static <T> T any(final Class<T> type) {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return null;
    }

    /**
     * Accepts any string as parameter.
     * @return a matcher that accepts any string parameter
     */
    public static String anyString() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return "";
    }

    /**
     * Accepts any int as parameter.
     * @return a matcher that accepts any int parameter
     */
    public static int anyInt() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return 42;
    }

    /**
     * Accepts any double as parameter.
     * @return a matcher that accepts any double parameter
     */
    public static double anyDouble() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return 42.0;
    }

    /**
     * Accepts any boolean as parameter.
     * @return a matcher that accepts any boolean parameter
     */
    public static boolean anyBoolean() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return true;
    }

    /**
     * Accepts any short as parameter.
     * @return a matcher that accepts any short parameter
     */
    public static short anyShort() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return (short) 42;
    }

    /**
     * Accepts any float as parameter.
     * @return a matcher that accepts any float parameter
     */
    public static float anyFloat() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return 42.0f;
    }

    /**
     * Accepts any byte as parameter.
     * @return a matcher that accepts any byte parameter
     */
    public static byte anyByte() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return (byte) 42;
    }

    /**
     * Accepts any char as parameter.
     * @return a matcher that accepts any char parameter
     */
    public static char anyChar() {
        SampleRepository.getInstance().addCurrentParameterMatchers(parameter -> true);
        return 'c';
    }

    /**
     * Accepts a parameter that equals to expectedParameter. Equality is tested using the .equals()-Method.
     * @param expectedParameter the expected value
     * @param <T> The type of the parameter that is expected.
     * @return A matcher that accepts a parameter that is equal to expectedParameter. Equality is tested using the .equals()-Method.
     */
    public static <T> T equalTo(final T expectedParameter) {
        SampleRepository.getInstance().addCurrentParameterMatchers(new EqualsMatcher<>(expectedParameter));
        return null;
    }

    /**
     * Accepts a parameter-object that is identical to expectedParameter. Equality is tests using the == operator.
     * @param expectedParameter the expected parameter object
     * @param <T> The type of the parameter that is expected.
     * @return a matcher that accepts a parameter object that is expected to be the same object as expectedParameter.
     */
    public static <T> T sameAs(final T expectedParameter) {
        SampleRepository.getInstance().addCurrentParameterMatchers(actualParameter -> actualParameter == expectedParameter);
        return null;
    }

    /**
     * Can be called with a custom {@link ParameterMatcher} to make DeepSampler remember for which position the
     * matcher should be used in the process.
     * @param matcher an implementation of {@link ParameterMatcher}
     * @param <T> type of matcher
     * @return matcher
     */
    public static <T> T matcher(final ParameterMatcher<T> matcher) {
        SampleRepository.getInstance().addCurrentParameterMatchers(matcher);
        return null;
    }

    /**
     * This Matcher is typically used by {@link Matchers#equalTo(Object)}, but since it is also used internally in various places
     * it is implemented as a class rather then a simple lambda, as it is the case with most of the Matchers.
     *
     * @param <T> The type of the objects that will be compared by this Matcher.
     */
    public static class EqualsMatcher<T> implements ParameterMatcher<T> {

        private final T expectedObject;

        public EqualsMatcher(final T expectedObject) {
            this.expectedObject = expectedObject;
        }

        @Override
        public boolean matches(final T parameter) {
            checkObjectImplementsEquals(parameter);
            return Objects.equals(expectedObject, parameter);
        }

        private void checkObjectImplementsEquals(final Object object) {
            if (object == null) {
                return;
            }

            try {
                object.getClass().getDeclaredMethod("equals", Object.class);
            } catch (final NoSuchMethodException e) {
                throw new InvalidConfigException("The class %s must implement equals() if you want to use an %s",
                        object.getClass().getName(),
                        EqualsMatcher.class.getName());
            }
        }
    }
}
