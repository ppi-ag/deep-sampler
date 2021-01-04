/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.core.model;

import java.util.function.Supplier;

/**
 * Defines the {@link SampleRepository}s scope by providing a container that holds the {@link SampleRepository}.
 * The {@link Scope} can be set using {@link SampleRepository#setScope(Scope)}.
 *
 * The default scope is {@link ThreadScope}, so Samples and all data associated with Samples are not
 * shared across different {@link Thread}s.
 */
public interface Scope {

    /**
     * Retrieves a {@link SampleRepository}, or creates a new one if no one exists yet. The implementation decides
     * where the SampleRepository is stored. E.g. If it is stored in a session scoped Bean, the SampleRepository is also
     * session scoped. If it is stored in a {@link ThreadLocal} the scope is thread scoped.
     *
     * The default scope is thread scope ({@link ThreadScope}.
     *
     * @param supplier If the current scope doesn't have a {@link SampleRepository} yet, this supplier is used to create a new one.
     * @return the {@link SampleRepository} of the current scope.
     */
    SampleRepository getOrCreate(Supplier<SampleRepository> supplier);

    /**
     * Cleans resources that might be referenced by a Scope. cleanUp() is called on a Scope that will be
     * replaced by a new Scope.
     */
    void cleanUp();

}
