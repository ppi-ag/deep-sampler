/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

/**
 * When using the persistence API you usually suffer from one major drawback: you are not able to define how the real parameters will get matched
 * with the recorded ones. When using the core API you would use {@link de.ppi.deepsampler.core.api.Matchers} for this purpose. In this case we can't use regular matchers
 * because the values we want to compare are <b>both</b> unknown. So we need another way to implement comparing logic. At this point the {@link PersistentMatcher}s come to our aid.
 * <br>
 * A {@link PersistentMatcher} will compare two objects of the same type and determine whether they match.
 * <br>
 * To use them it will be necessary to create a {@link ComboMatcher} with {@link PersistentMatchers#combo(Object, PersistentMatcher)}.
 * These {@link ComboMatcher}s can be used like a regular {@link de.ppi.deepsampler.core.model.ParameterMatcher}
 *
 * @see ComboMatcher
 * @see PersistentMatchers
 * @param <T> type to compare
 * @author Rico Schrage
 */
public interface PersistentMatcher<T> {
    /**
     * Compares two values in an arbitrary way with each other.
     *
     * @param first the first value (real value in the execution of the test)
     * @param second the second value (the persistent constant value)
     * @return true if the objects should be considered as matching, false otherwise
     */
    boolean matches(T first, T second);
}
