package org.deepmock.core.internal.handler;

import javassist.util.proxy.MethodHandler;
import org.deepmock.core.api.Quantity;

import java.lang.reflect.Method;

public class VerifyBehaviorHandler implements MethodHandler {
    private final Quantity quantity;
    private final Class<?> cls;

    public VerifyBehaviorHandler(Quantity quantity, Class<?> cls) {
        this.quantity = quantity;
        this.cls = cls;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return null;
    }
}
