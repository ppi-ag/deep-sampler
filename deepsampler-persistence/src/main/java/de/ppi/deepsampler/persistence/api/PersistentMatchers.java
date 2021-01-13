/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

import de.ppi.deepsampler.core.model.ParameterMatcher;

public class PersistentMatchers {

    public static <T> ComboMatcher<T> combo(ParameterMatcher<T> parameterMatcher, PersistentMatcher<T> persistentMatcher) {
        return new ComboMatcher<>(parameterMatcher, persistentMatcher);
    }

}
