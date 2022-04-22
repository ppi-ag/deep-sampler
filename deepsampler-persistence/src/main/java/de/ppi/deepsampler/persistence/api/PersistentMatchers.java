/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

import de.ppi.deepsampler.core.api.MatcherTools;
import de.ppi.deepsampler.core.model.ParameterMatcher;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.persistence.error.PersistenceException;

import java.util.Objects;

/**
 * This class is a collection of factory methods for some different types of {@link PersistentMatcher}.
 * <br>
 * Furthermore it offers a factory method for creating {@link ComboMatcher}s. Whenever you want to use {@link PersistentMatcher} it will be necessary to wrap them in a {@link ComboMatcher} with
 * the factory method {@link #combo(Object, PersistentMatcher)}. After that you can use the {@link ComboMatcher} like any other {@link ParameterMatcher}. The same rules, which are valid for any
 * {@link ParameterMatcher} are applied to {@link ComboMatcher} as well.
 *
 * @author Rico Schrage
 * @see de.ppi.deepsampler.core.api.Matchers
 */
public class PersistentMatchers {

    private PersistentMatchers() {
        // static only
    }

    /**
     * Matches parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All parameters are accepted.</li>
     *     <li>Testing: The parameters, that actually appear during testing, are matched against recorded parameters. {@link Object#equals(Object)} is used
     *     to test if actual parameter objects match to recorded parameter objects.</li>
     * </ul>
     * <p>
     * An Exception is thrown, if the parameter-object does not override {@link Object#equals(Object)}. In this case
     * {@link PersistentMatchers#anyRecorded(PersistentMatcher)} can be used to define a {@link PersistentMatcher}, that will be used instead
     * of {@link Object#equals(Object)}.
     *
     * @param <T> The type of the parameter that will be accepted
     * @return a matcher that accepts any recorded parameter value
     */
    public static <T> T anyRecorded() {
        anyRecorded(new EqualsMatcher<>());
        return null;
    }

    /**
     * Matches parameters during recording and during tests, that use previously recorded samples.
     * <p>
     * This method essentially does the same thing as {@link #anyRecorded()}. However, if the method for which parameter matchers
     * shall be defined is overloaded in a way, that the generic return type of {@link #anyRecorded()} cannot be resolved without
     * a cast, this method allows to definde the type of the parameter, so that the generic return type is resolvable.
     *
     * @param <T>  The type of the parameter that will be accepted
     * @param type The {@link Class} of the parameter that will be accepted independently of its concrete value.
     * @return a matcher that accepts any recorded parameter value
     */
    public static <T> T anyRecorded(final Class<T> type) {
        Objects.requireNonNull(type);
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return null;
    }

    /**
     * Matches string parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All strings are accepted.</li>
     *     <li>Testing: The strings, that actually appear during testing, are matched against recorded parameters. {@link Object#equals(Object)} is used
     *     to test if actual parameter objects match to recorded parameter objects.</li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded Strings.
     */
    public static String anyRecordedString() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return "42";
    }

    /**
     * Matches int parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All int parameters are accepted.</li>
     *     <li>Testing: The ints, that actually appear during testing, are matched against recorded parameters.</li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded ints.
     */
    public static int anyRecordedInt() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return 42;
    }

    /**
     * Matches long parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All long parameters are accepted.</li>
     *     <li>Testing: The long parameters, that actually appear during testing, are matched against recorded parameters. </li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded long-values.
     */
    public static long anyRecordedLong() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return 42L;
    }

    /**
     * Matches double parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All double parameters are accepted.</li>
     *     <li>Testing: The double parameters, that actually appear during testing, are matched against recorded parameters. </li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded double-values.
     */
    public static double anyRecordedDouble() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return 42.0;
    }

    /**
     * Matches float parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All float parameters are accepted.</li>
     *     <li>Testing: The float parameters, that actually appear during testing, are matched against recorded parameters. </li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded float-values.
     */
    public static float anyRecordedFloat() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return 2.0F;
    }

    /**
     * Matches char parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All char parameters are accepted.</li>
     *     <li>Testing: The char parameters, that actually appear during testing, are matched against recorded parameters. </li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded chars.
     */
    public static char anyRecordedChar() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return '4';
    }

    /**
     * Matches boolean parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All boolean parameters are accepted.</li>
     *     <li>Testing: The boolean parameters, that actually appear during testing, are matched against recorded parameters. </li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded booleans.
     */
    public static boolean anyRecordedBoolean() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return false;
    }

    /**
     * Matches short parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All short parameters are accepted.</li>
     *     <li>Testing: The short parameters, that actually appear during testing, are matched against recorded parameters. </li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded shorts.
     */
    public static short anyRecordedShort() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return (short) 42;
    }

    /**
     * Matches byte parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All byte parameters are accepted.</li>
     *     <li>Testing: The byte parameters, that actually appear during testing, are matched against recorded parameters. </li>
     * </ul>
     *
     * @return A Matcher, that accepts recorded bytes.
     */
    public static byte anyRecordedByte() {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, new EqualsMatcher<>()));
        return 0xf;
    }

    /**
     * Matches all types of parameters during recording and during tests, that use previously recorded samples.
     *
     * <ul>
     *     <li>Recording: All parameters are accepted.</li>
     *     <li>Testing: The parameters, that actually appear during testing, are matched against recorded parameters using a custom {@link PersistentMatcher}.
     *     This is especially useful, if the parameter objects do not override {@link Object#equals(Object)}. The {@link PersistentMatcher} can be used
     *     to replace the equals() in these cases. The {@link PersistentMatcher} may conveniently be defined as a lambda or a method reference. </li>
     * </ul>
     *
     * @param persistentMatcher A replacement for a missing {@link Object#equals(Object)}-method.
     * @param <T>               The type of the parameter that will be accepted
     * @return A matcher using the passed {@link PersistentMatcher}
     */
    public static <T> T anyRecorded(final PersistentMatcher<T> persistentMatcher) {
        SampleRepository.getInstance().addCurrentParameterMatchers(new ComboMatcher<>(parameter -> true, persistentMatcher));
        return null;
    }

    /**
     * Allows adding separate matchers for loading and recording of samples.
     * <p>
     * Please notice: The basic idea of persistent samples is, that the parameters, with which a stubbed method is
     * called during a test, are compared to previously recorded parameters. So usually the playingMatcher is used
     * to compare the expected parameters from the sample-file with the actual parameters during test.
     * This method allows to change this behavior. We recommend using this method only after careful considerations.
     *
     * @param recordingMatcher this macher will be used during recording of samples. The matcher can be created using the
     *                         helper-methods in {@link de.ppi.deepsampler.core.api.Matchers}
     * @param playingMatcher   this matcher will be used during replay.
     * @param <T>              type to compare/match
     * @return always returns null
     */
    @SuppressWarnings("unchecked")
    public static <T> T combo(final T recordingMatcher, final PersistentMatcher<T> playingMatcher) {
        Objects.requireNonNull(recordingMatcher, "recordingMatcher must not be null.");

        if (SampleRepository.getInstance().getCurrentParameterMatchers().isEmpty()) {
            throw new PersistenceException("It wasn't possible to retrieve the last ParameterMatcher. Did you passed a ParameterMatcher created with a static factory method in de.ppi.deepsampler.core.api.Matchers?");
        }

        SampleRepository.getInstance().setCurrentParameterMatchers(new ComboMatcher<>((ParameterMatcher<T>) SampleRepository.getInstance().getLastParameterMatcher(), playingMatcher));
        return null;
    }

    /**
     * Create a {@link PersistentMatcher} for comparing two objects using their equals method.
     *
     * @param <T> type to compare
     * @return the created {@link PersistentMatcher}
     */
    public static <T> PersistentMatcher<T> equalsMatcher() {
        return new EqualsMatcher<>();
    }

    /**
     * Create a {@link PersistentMatcher} for comparing two objects by memory address (objOne == objTwo).
     *
     * @param <T> type to compare
     * @return the {@link PersistentMatcher}
     */
    public static <T> PersistentMatcher<T> sameMatcher() {
        return (first, second) -> first == second;
    }

    /**
     * This Matcher is typically used by {@link PersistentMatchers#equalsMatcher()}, but since it is also used internally in various places
     * it is implemented as a class rather than a simple lambda, as it is the case with most of the Matchers.
     *
     * @param <T> The type of the objects that will be compared by this Matcher.
     */
    public static class EqualsMatcher<T> implements PersistentMatcher<T> {


        @Override
        public boolean matches(final T first, final T second) {
            MatcherTools.checkObjectOverridesEquals(first);
            MatcherTools.checkObjectOverridesEquals(second);

            return Objects.equals(first, second);
        }
    }
}
