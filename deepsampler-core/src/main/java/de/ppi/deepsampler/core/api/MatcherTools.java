/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.InvalidConfigException;

import java.lang.reflect.Method;

/**
 * A small collection of commonly used utilities for Matchers.
 */
public class MatcherTools {

    private MatcherTools() {
        // Since this is a Utility class, instantiation is not intended.
    }

    /**
     * Checks if object or one of its parent classes overrides {@link Object#equals(Object)}. If this is not the case, an 
     * {@link InvalidConfigException} is thrown.
     * 
     * @param object The object that is expected to provide a custom implementation of {@link Object#equals(Object)}
     */
    public static void checkObjectOverridesEquals(final Object object) {
        if (object == null) {
            return;
        }

        try {
            var equals = object.getClass().getMethod("equals", Object.class);

            if (equals.getDeclaringClass().equals(Object.class)) {
                complainAboutMissingEqualsMethod(object);
            }
        } catch (final NoSuchMethodException e) {
            complainAboutMissingEqualsMethod(object);
        }
    }

    private static void complainAboutMissingEqualsMethod(Object object) {
        throw new InvalidConfigException("The class %s must implement equals() if you want to use an %s",
                object.getClass().getName(),
                Matchers.EqualsMatcher.class.getName());
    }
}
