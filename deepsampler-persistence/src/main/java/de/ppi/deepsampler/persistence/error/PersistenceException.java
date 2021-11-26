/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.error;

import de.ppi.deepsampler.core.error.BaseException;

public class PersistenceException extends BaseException {

    /**
     * Creates an {@link PersistenceException} with a message.
     * @param message The message may contain placeholders like %s for Strings, or %d for decimal values.
     *                See {@link String#format(String, Object...)}.
     *                The placeholders are filled with values from parameter args.
     * @param args the args that are used to fill placeholders in message.
     */
    public PersistenceException(final String message, final Object... args) {
        super(message, args);
    }

    /**
     * Creates an {@link PersistenceException} with a message.
     * @param message The message may contain placeholders like %s for Strings, or %d for decimal values.
     *                See {@link String#format(String, Object...)}.
     *                The placeholders are filled with values from parameter args.
     * @param cause The original {@link Exception}
     * @param args he args that are used to fill placeholders in message.
     */
    public PersistenceException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }
}
