/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

@FunctionalInterface
public interface ParameterMatcher<T> {
    boolean matches(T parameter);
}
