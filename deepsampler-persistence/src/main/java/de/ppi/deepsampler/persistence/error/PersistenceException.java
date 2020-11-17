package de.ppi.deepsampler.persistence.error;

import de.ppi.deepsampler.core.error.BaseException;

public class PersistenceException extends BaseException {

    public PersistenceException(final String message, final Object... args) {
        super(message, args);
    }

    public PersistenceException(final String message, final Throwable cause, final Object... args) {
        super(message, cause, args);
    }
}
