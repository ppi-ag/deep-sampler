/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.model.ExecutionRepository;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SingletonScope;
import de.ppi.deepsampler.core.model.ThreadScope;

/**
 * Provides functionality for influencing the execution phase of the stubbing done by deepsampler.
 *
 * @author Rico Schrage
 */
public class Execution {

    private Execution() {
        // static only
    }

    /**
     * Makes deepsampler use a provided {@link SampleReturnProcessor} when returning arbitrary stubbed data.
     *
     * @param sampleReturnProcessor the sampleReturnProcessor
     */
    public static void useGlobal(SampleReturnProcessor sampleReturnProcessor) {
        ExecutionRepository.getInstance().addGlobalSampleReturnProcessor(sampleReturnProcessor);
    }

    /**
     * Makes deepsampler use a provided {@link SampleReturnProcessor} when returning stubbed data defined by the last created sample.
     *
     * @param sampleReturnProcessor the sampleReturnProcessor
     */
    public static void useForLastSample(SampleReturnProcessor sampleReturnProcessor) {
        ExecutionRepository.getInstance().addSampleReturnProcessor(SampleRepository.getInstance().getLastSampleDefinition(), sampleReturnProcessor);
    }

    /**
     * Set the scope for all samples created with deepsampler.
     *
     * @see ScopeType, {@link de.ppi.deepsampler.core.model.Scope}
     * @param type the type of the scope you want to set
     */
    public static void setScope(ScopeType type) {
        if (type == ScopeType.SINGLETON) {
            ExecutionRepository.setScope(new SingletonScope<>());
            SampleRepository.setScope(new SingletonScope<>());
        } else if (type == ScopeType.THREAD) {
            ExecutionRepository.setScope(new ThreadScope<>());
            SampleRepository.setScope(new ThreadScope<>());
        } else {
            throw new InvalidConfigException("The scope type %s is not supported!", type);
        }
    }
}
