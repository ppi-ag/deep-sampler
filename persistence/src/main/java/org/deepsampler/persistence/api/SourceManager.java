package org.deepsampler.persistence.api;

import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.persistence.model.PersistentModel;

import java.util.Map;

/**
 * <p>
 * A SourceManager is responsible for interacting with the actual persistent source.
 * </p>
 *
 * <p>
 *     You will have to define two methods:
 *     <ul>
 *         <li>record(...): Basically you have to write the provided data to your data source. There is no constraint on
 *         how you do it, you just should write it in a form so that you are able to load it correctly again.</li>
 *         <li>load(...): Load your data and transform your data to an implementation of {@link PersistentModel}</li>
 *     </ul>
 * </p>
 */
public interface SourceManager {

    /**
     * Record the executionInformation collected on runtime to your data source.
     *
     * @param executionInformation the executionInformation mapped by class
     */
    void record(Map<Class<?>, ExecutionInformation> executionInformation);

    /**
     * Load the data you wrote. You will also have to transform this (if not already happened by design) to an
     * implementation of {@link PersistentModel}.
     *
     * @return PersistentModel
     */
    PersistentModel load();
}
