/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

import de.ppi.deepsampler.core.model.ParameterMatcher;

public class ComboMatcher<T> implements ParameterMatcher<T> {

    private final PersistentMatcher<T> persistentMatcher;
    private final ParameterMatcher<T> parameterMatcher;

    public ComboMatcher(ParameterMatcher<T> parameterMatcher, PersistentMatcher<T> persistentMatcher) {
        this.parameterMatcher = parameterMatcher;
        this.persistentMatcher = persistentMatcher;
    }

    public PersistentMatcher<T> getPersistentMatcher() {
        return persistentMatcher;
    }

    public ParameterMatcher<T> getParameterMatcher() {
        return parameterMatcher;
    }

    @Override
    public boolean matches(T parameter) {
        return parameterMatcher.matches(parameter);
    }
}
