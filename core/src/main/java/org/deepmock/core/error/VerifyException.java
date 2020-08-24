package org.deepmock.core.error;

import org.deepmock.core.model.JoinPoint;

public class VerifyException extends BaseException {

    public VerifyException(JoinPoint joinPoint, int expected, int actual) {
        super("The join point %s was " +
                "expected to get invoked %s times, " +
                "actually it got invoked %s times", joinPoint.toString(), expected, actual);
    }

}
