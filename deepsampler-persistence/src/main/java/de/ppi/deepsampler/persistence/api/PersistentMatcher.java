/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

public interface PersistentMatcher<T> {
    boolean matches(T first, T second);
}
