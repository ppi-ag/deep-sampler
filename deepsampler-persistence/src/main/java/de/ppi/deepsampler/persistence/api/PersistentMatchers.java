/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
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
 * the factory method {@link #combo(Object, PersistentMatcher)}. After that you can use the {@link ComboMatcher} like any other {@link ParameterMatcher}. The same rules which are valid for any
 * {@link ParameterMatcher} are applied to {@link ComboMatcher} as well.
 *
 * @see de.ppi.deepsampler.core.api.Matchers
 * @author Rico Schrage
 */
public class PersistentMatchers {

    private PersistentMatchers() {
        // static only
    }

    /**
     * Create a {@link ComboMatcher} with respect of making it available for the current {@link de.ppi.deepsampler.core.model.SampleDefinition}.
     *
     * @param parameterMatcher the parameterMatcher to be imitated by the {@link ComboMatcher}
     * @param persistentMatcher the persistentMatcher to be used as comparator (real value <-> persistent value)
     * @param <T> type to compare/match
     *
     * @return always returns null
     */
    @SuppressWarnings("unchecked")
    public static <T> T combo(T parameterMatcher, PersistentMatcher<T> persistentMatcher) {
        if (SampleRepository.getInstance().getCurrentParameterMatchers().isEmpty()) {
            throw new PersistenceException("It wasn't possible to retrieve the last ParameterMatcher. Did you passed a ParameterMatcher created with a static factory method in de.ppi.deepsampler.core.api.Matchers?");
        }

        SampleRepository.getInstance().setCurrentParameterMatchers(new ComboMatcher<>((ParameterMatcher<T>) SampleRepository.getInstance().getLastParameterMatcher(), persistentMatcher));
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
