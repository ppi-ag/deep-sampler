/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.model.ExecutionRepository;
import de.ppi.deepsampler.core.model.SampleRepository;

/**
 * Provides functionality for influencing the execution phase of the stubbing done by deepsampler.
 *
 * @author Rico Schrage
 */
public class Execution {

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
}
