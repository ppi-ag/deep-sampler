/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.core.model;

import java.util.function.Supplier;

/**
 * A thread scope for the {@link SampleRepository}. If this scope is used, Samples and all associated data cannoit be
 * shared across separated {@link Thread}s.
 *
 * This is the default scope.
 *
 * The scope can be changed using {@link SampleRepository#setScope(SampleRepositoryScope)}.
 */
public class SampleRepositoryThreadScope implements SampleRepositoryScope {

    private final ThreadLocal<SampleRepository> sampleRepository = new ThreadLocal<>();


    /**
     * Delivers the SampleRepository of the current {@link Thread} or creates a new one if the current {@link Thread} doesn't
     * have a {@link SampleRepository}.
     *
     * @param supplier A Supplier that is used to create a new {@link SampleRepository} if the current {@link Thread} doesn't have one.
     * @return The {@link SampleRepository} of the current {@link Thread}.
     */
    @Override
    public synchronized SampleRepository getOrCreate(Supplier<SampleRepository> supplier) {
        if (sampleRepository.get() == null) {
            sampleRepository.set(supplier.get());
        }

        return sampleRepository.get();
    }
}
