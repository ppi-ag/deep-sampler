/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.core.model;

import java.util.function.Supplier;

/**
 * A thread scope for an arbitrary object. If this scope is used, Samples and all associated data cannot be
 * shared across separated {@link Thread}s.
 *
 * This is the default scope.
 *
 * The scope can be changed using {@link de.ppi.deepsampler.core.api.Execution#setScope(de.ppi.deepsampler.core.api.ScopeType)}.
 */
public class ThreadScope<T> implements Scope<T> {

    private final ThreadLocal<T> sampleRepository = new ThreadLocal<>();


    /**
     * Delivers the hold object of the current {@link Thread} or creates a new one if the current {@link Thread} doesn't
     * have an instance yet.
     *
     * @param supplier A Supplier that is used to create a new object of the hold class if the current {@link Thread} doesn't have one.
     * @return The hold instance of the current {@link Thread}.
     */
    @Override
    public synchronized T getOrCreate(Supplier<T> supplier) {
        if (sampleRepository.get() == null) {
            sampleRepository.set(supplier.get());
        }

        return sampleRepository.get();
    }

}
