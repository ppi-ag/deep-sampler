package org.deepsampler.persistence.json.error;

import org.deepsampler.core.error.BaseException;

public class PersistenceException extends BaseException {

    public PersistenceException(String message, Object... args) {
        super(message, args);
    }

    public PersistenceException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }
}
