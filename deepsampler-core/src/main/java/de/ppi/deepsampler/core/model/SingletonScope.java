/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.core.model;

import java.util.function.Supplier;

/**
 * A singleton scope for the repositories in deepsampler. If this scope is used,  Samples and all associated data are
 * shared across separated {@link Thread}s.
 *
 * The default scope is {@link ThreadScope}.
 *
 * The scope can be changed using {@link de.ppi.deepsampler.core.api.Execution#setScope(de.ppi.deepsampler.core.api.ScopeType)}.
 */
public class SingletonScope<T> implements Scope<T> {

    private T sampleRepository;

    /**
     * Delivers the global object hold by the scope or creates a new one if none exists yet.
     *
     * @param supplier A Supplier that is used to create a new object of the hold class if non exists yet.
     * @return The global {@link SampleRepository}.
     */
    @Override
    public synchronized T getOrCreate(Supplier<T> supplier) {
        if (sampleRepository == null) {
            sampleRepository = supplier.get();
        }

        return sampleRepository;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
