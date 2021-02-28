/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.core.model;

import java.util.function.Supplier;

/**
 * Defines the a scope by providing a container that holds the Repositories used by deepsampler.
 * The {@link Scope} can be set using {@link de.ppi.deepsampler.core.api.Execution#setScope(de.ppi.deepsampler.core.api.ScopeType)}.
 *
 * The default scope is {@link ThreadScope}, so Samples and all data associated with Samples are not
 * shared across different {@link Thread}s.
 */
public interface Scope<T> {

    /**
     * Retrieves the hold object, or creates a new one if no one exists yet. The implementation decides
     * where the object is stored. E.g. If it is stored in a session scoped Bean, the object is also
     * session scoped. If it is stored in a {@link ThreadLocal} the scope is thread scoped.
     *
     * The default scope is thread scope ({@link ThreadScope}.
     *
     * @param supplier If the current scope doesn't have an instance of the hold class yet, this supplier is used to create a new one.
     * @return T the instance hold by the current scope.
     */
    T getOrCreate(Supplier<T> supplier);

}
