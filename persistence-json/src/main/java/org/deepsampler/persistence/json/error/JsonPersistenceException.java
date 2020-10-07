package org.deepsampler.persistence.json.error;

import org.deepsampler.persistence.error.PersistenceException;

public class JsonPersistenceException extends PersistenceException {

    public JsonPersistenceException(String message, Object... args) {
        super(message, args);
    }

    public JsonPersistenceException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }
}
