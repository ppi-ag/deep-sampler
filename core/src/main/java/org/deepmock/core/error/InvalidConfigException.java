package org.deepmock.core.error;

public class InvalidConfigException extends BaseException {

    public InvalidConfigException(String message, Object... args) {
        super(message, args);
    }

    public InvalidConfigException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }
}
