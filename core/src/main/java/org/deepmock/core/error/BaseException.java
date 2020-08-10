package org.deepmock.core.error;

public class BaseException extends RuntimeException {

    public BaseException(String message, Object... args) {
        super(String.format(message, args));
    }

    public BaseException(String message, Throwable cause, Object... args) {
        super(String.format(message, args), cause);
    }
}
