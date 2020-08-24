package org.deepmock.core.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodCall {
    private final List<Object> args;
    private final Object returnValue;

    public MethodCall(Object returnValue, Object... args) {
        this.args = Arrays.asList(args);
        this.returnValue = returnValue;
    }

    public List<Object> getArgs() {
        return Collections.unmodifiableList(args);
    }

    public Object getReturnValue() {
        return returnValue;
    }
}
