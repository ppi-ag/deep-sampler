package org.deepmock.persistence.error;

import org.deepmock.core.error.BaseException;

public class PersistenceException extends BaseException {

    public PersistenceException(String message, Object... args) {
        super(message, args);
    }

    public PersistenceException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }
}
