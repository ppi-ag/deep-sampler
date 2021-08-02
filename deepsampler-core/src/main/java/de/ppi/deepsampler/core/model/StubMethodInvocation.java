/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.util.List;

/**
 * Describes the actual call of a stubbed method.
 */
public class StubMethodInvocation {

    private final List<Object> parameters;
    private final Object stubInstance;
    private final OriginalMethodInvocation originalMethodInvocation;

    public StubMethodInvocation(final List<Object> parameters, final Object stubInstance, final OriginalMethodInvocation originalMethodInvocation) {
        this.parameters = parameters;
        this.stubInstance = stubInstance;
        this.originalMethodInvocation = originalMethodInvocation;
    }

    /**
     * If the called method is defined with parameters, the values of these parameters can be accessed using this getter.
     *
     * @return The parameter values of a particular method call. The {@link List} is empty if the method has no parameters.
     */
    public List<Object> getParameters() {
        return parameters;
    }


    /**
     * Gives access to the instance of the stubbed class where the current method is called on.
     *
     * @return The instance of the stub
     * @param <T> The type of the stub
     */
    @SuppressWarnings("unchecked")
    public <T> T getStubInstance() {
        return (T) stubInstance;
    }

    /**
     * Calls the original method and returns the return value from the original method, if it produced any.
     *
     * @return If the original method is a void method, null is returned, otherwise the value that was returned by the original method is returned.
     * @throws  Throwable  The underlying AOP-Libraries will most likely declare {@link Throwable} on the method that calls the original method. At least
     * this is the case with spring and guice-aop. This is why we have to relay {@link Throwable} here, even though this might appear most unusual.
     */
    public Object callOriginalMethod() throws Throwable {
        return originalMethodInvocation.call();
    }
}
