/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

/**
 * A {@link FunctionalInterface} that is used to pass a means of calling the original method, to an {@link Answer}.
 */
@FunctionalInterface
public interface OriginalMethodInvocation {

    /**
     * Calls the original method using the particular means of the particular AOP-framework that is used for stubbing.
     * @return If the original method is a void method, null is returned. Otherwise the return value from the original method
     * is returned.
     *
     * @throws Throwable The underlying AOP-Libraries will most likely declare {@link Throwable} on the method that calls the original method. At least
     *        this is the case with spring and guice-aop. This is why we have to relay {@link Throwable} here, even though this might appear most unusual.
     */
    @SuppressWarnings("java:S112") // Ignore complaints about throws Throwable since this is the result of the AOP APIs which are called by this Method
    Object call() throws Throwable;

}
