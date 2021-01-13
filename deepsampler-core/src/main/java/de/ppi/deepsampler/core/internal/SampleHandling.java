/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.internal;

import de.ppi.deepsampler.core.model.ParameterMatcher;
import de.ppi.deepsampler.core.model.SampleDefinition;

import java.util.List;

public class SampleHandling {

    @SuppressWarnings("unchecked")
    public static boolean argumentsMatch(final SampleDefinition sampleDefinition, final Object[] arguments) {
        final List<ParameterMatcher<?>> parameterMatchers = sampleDefinition.getParameterMatchers();

        if (parameterMatchers.size() != arguments.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            final ParameterMatcher<Object> parameterMatcher = (ParameterMatcher<Object>) parameterMatchers.get(i);
            if (!parameterMatcher.matches(arguments[i])) {
                return false;
            }
        }

        return true;
    }
}
