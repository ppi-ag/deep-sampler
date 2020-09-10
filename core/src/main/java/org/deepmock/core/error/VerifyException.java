package org.deepmock.core.error;

import org.deepmock.core.model.SampledMethod;

public class VerifyException extends BaseException {

    public VerifyException(SampledMethod sampledMethod, int expected, int actual) {
        super("The join point %s was " +
                "expected to get invoked %s times, " +
                "actually it got invoked %s times", sampledMethod.toString(), expected, actual);
    }

}
