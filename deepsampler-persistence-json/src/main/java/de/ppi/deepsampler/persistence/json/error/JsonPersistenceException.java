/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.error;

import de.ppi.deepsampler.persistence.error.PersistenceException;

public class JsonPersistenceException extends PersistenceException {

    public JsonPersistenceException(final String message, final Object... args) {
        super(message, args);
    }

    public JsonPersistenceException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }
}
