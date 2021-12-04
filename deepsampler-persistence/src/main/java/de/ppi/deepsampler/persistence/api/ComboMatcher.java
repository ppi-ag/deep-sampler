/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

import de.ppi.deepsampler.core.model.ParameterMatcher;

/**
 * Special matcher to apply a {@link ParameterMatcher} <b>and</b> a {@link PersistentMatcher} to a single argument when defining a sample.
 * <br>
 * This class will basically behave exactly like the given {@link ParameterMatcher}, but additionally it will hold a {@link PersistentMatcher} to
 * retrieve it later in the loading process of persistent samples.
 * <br>
 * <b>Never create {@link ComboMatcher} yourself! Always use {@link PersistentMatchers#combo(Object, PersistentMatcher)} for this.</b>
 *
 * @param <T> type to get matched in some sense
 * @author Rico Schrage
 */
public class ComboMatcher<T> implements ParameterMatcher<T> {

    private final PersistentMatcher<T> persistentMatcher;
    private final ParameterMatcher<T> parameterMatcher;

    /**
     * Create a ComboMatcher with the parameterMatcher to be imitated and the persistentMatcher to hold for the later creating of a real matcher
     * in the process of loading persistent samples.
     *
     * @param parameterMatcher the {@link ParameterMatcher} to imitate
     * @param persistentMatcher the {@link PersistentMatcher} to hold
     */
    ComboMatcher(ParameterMatcher<T> parameterMatcher, PersistentMatcher<T> persistentMatcher) {
        this.parameterMatcher = parameterMatcher;
        this.persistentMatcher = persistentMatcher;
    }

    /**
     * @return the hold {@link PersistentMatcher}
     */
    public PersistentMatcher<T> getPersistentMatcher() {
        return persistentMatcher;
    }

    /**
     * @return the imitated {@link ParameterMatcher}
     */
    public ParameterMatcher<T> getParameterMatcher() {
        return parameterMatcher;
    }

    @Override
    public boolean matches(T parameter) {
        return parameterMatcher.matches(parameter);
    }
}
