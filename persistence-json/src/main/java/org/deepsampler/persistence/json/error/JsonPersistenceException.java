package org.deepsampler.persistence.json.error;

import org.deepsampler.persistence.error.PersistenceException;

public class JsonPersistenceException extends PersistenceException {

    public JsonPersistenceException(final String message, final Object... args) {
        super(message, args);
    }

    public JsonPersistenceException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }
}
