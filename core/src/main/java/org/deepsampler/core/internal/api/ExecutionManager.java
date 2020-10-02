package org.deepsampler.core.internal.api;

import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.core.model.ExecutionRepository;

public class ExecutionManager {

    public static void notify(SampleDefinition sampleDefinition) {
        ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(sampleDefinition.getSampledMethod().getTarget());
        executionInformation.getOrCreateBySample(sampleDefinition).increaseTimesInvoked();
    }

}
