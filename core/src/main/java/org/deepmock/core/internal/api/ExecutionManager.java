package org.deepmock.core.internal.api;

import org.deepmock.core.model.SampleDefinition;
import org.deepmock.core.model.ExecutionInformation;
import org.deepmock.core.model.ExecutionRepository;

public class ExecutionManager {

    public static void notify(SampleDefinition behavior) {
        ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(behavior.getSampledMethod().getTarget()
        );
        executionInformation.getOrCreateBySample(behavior).increaseTimesInvoked();
    }

}
