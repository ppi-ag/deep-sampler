/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

/**
 * Represents a call of a void-method. This functional interface is intended to call methods an Sampler only.
 *
 * @param <E> an Exception that might be thrown by the called void method.
 */
@FunctionalInterface
public interface VoidCall<E extends Exception> {

    /**
     * Calls a void method an a Sampler.
     * @throws E The Exception that might be thrown by the void method.
     */
    void call() throws E;
}
