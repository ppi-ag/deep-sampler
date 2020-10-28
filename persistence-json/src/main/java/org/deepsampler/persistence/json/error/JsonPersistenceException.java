package org.deepsampler.persistence.json.error;

public class JsonPersistenceException extends PersistenceException {

    public JsonPersistenceException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }
}
