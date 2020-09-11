package org.deepsampler.core.internal.api;

import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.core.model.ExecutionRepository;

public class ExecutionManager {

    public static void notify(SampleDefinition behavior) {
        ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(behavior.getSampledMethod().getTarget()
        );
        executionInformation.getOrCreateBySample(behavior).increaseTimesInvoked();
    }

}
