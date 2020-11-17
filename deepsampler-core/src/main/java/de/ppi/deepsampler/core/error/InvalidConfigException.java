/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.error;

public class InvalidConfigException extends BaseException {

    public InvalidConfigException(final String message, final Object... args) {
        super(message, args);
    }

}
