/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.core.model;

import java.util.function.Supplier;

/**
 * A singleton scope for the {@link SampleRepository}. If this scope is used,  Samples and all associated data are
 * shared across separated {@link Thread}s.
 *
 * The default scope is {@link ThreadScope}.
 *
 * The scope can be changed using {@link SampleRepository#setScope(Scope)}.
 */
public class SingletonScope implements Scope {

    private static SampleRepository sampleRepository;

    /**
     * Delivers the global SampleRepository or creates a new one if none exists yet.
     *
     * @param supplier A Supplier that is used to create a new {@link SampleRepository} if non exists yet.
     * @return The global {@link SampleRepository}.
     */
    @Override
    public synchronized SampleRepository getOrCreate(Supplier<SampleRepository> supplier) {
        if (sampleRepository == null) {
            sampleRepository = supplier.get();
        }

        return sampleRepository;
    }

    @Override
    public void cleanUp() {
        sampleRepository = null;
    }
}
