/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.error.BaseException;

/**
 * An Exception that is used for technical errors within the junit-plugins.
 */
public class JUnitPreparationException extends BaseException {

    /**
     * Creates a new Exception.
     * @param message The Message may contain format specifier and arguments as defined by {@link String#format(String, Object...)}.
     *                The arguments are filled with values from args.
     * @param cause The original Exception
     * @param args If message contains arguments (like %s for Strings) the values of these arguments are taken from this args[]
     */
    public JUnitPreparationException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }

    public JUnitPreparationException(final String message, final Object... args) {
        super(message, args);
    }

}
