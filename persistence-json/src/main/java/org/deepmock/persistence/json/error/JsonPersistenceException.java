package org.deepmock.persistence.json.error;

import org.deepmock.core.error.BaseException;

public class JsonPersistenceException extends BaseException {

    public JsonPersistenceException(String message, Object... args) {
        super(message, args);
    }

    public JsonPersistenceException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }
}
