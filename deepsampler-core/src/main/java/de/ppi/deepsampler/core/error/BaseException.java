/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.error;

public class BaseException extends RuntimeException {

    /**
     * Creates an Exception with a message.
     * @param message The message may contain placeholders like %s for Strings, or %d for decimal values.
     *                See {@link String#format(String, Object...)}.
     *                The placeholders are filled with values from parameter args.
     * @param args the args that are used to fill placeholders in message.
     */
    public BaseException(final String message, final Object... args) {
        super(String.format(message, args));
    }

    /**
     * Creates an Exception with a message.
     * @param message The message may contain placeholders like %s for Strings, or %d for decimal values.
     *                See {@link String#format(String, Object...)}.
     *                The placeholders are filled with values from parameter args.
     * @param cause The original {@link Exception}
     * @param args he args that are used to fill placeholders in message.
     */
    public BaseException(final String message, final Throwable cause, final Object... args) {
        super(String.format(message, args), cause);
    }
}
