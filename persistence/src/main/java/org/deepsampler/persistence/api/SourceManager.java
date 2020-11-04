package org.deepsampler.persistence.api;

import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.persistence.PersistentSamplerContext;
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
 *         <li>save(...): Basically you have to write the provided data to your data source. There is no constraint on
 *         how you do it, you just should write it in a form so that you are able to load it correctly again.</li>
 *         <li>load(...): Load your data and transform your data to an implementation of {@link PersistentModel}</li>
 *     </ul>
 * </p>
 */
public interface SourceManager {

    /**
     * Save the executionInformation collected on runtime to your data source.
     *
     * @param executionInformation the executionInformation mapped by class
     * @param persistentSamplerContext context of the persistent sampler
     */
    void save(Map<Class<?>, ExecutionInformation> executionInformation, PersistentSamplerContext persistentSamplerContext);

    /**
     * Load the data you wrote. You will also have to transform this (if not already happened by design) to an
     * implementation of {@link PersistentModel}.
     *
     * @param persistentSamplerContext context of the persistent sampler
     * @return PersistentModel
     */
    PersistentModel load(PersistentSamplerContext persistentSamplerContext);
}
