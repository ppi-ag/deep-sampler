package org.deepsampler.core.internal.api;

import org.deepsampler.core.model.*;

public class ExecutionManager {

    public static void notify(SampleDefinition sampleDefinition) {
        getSampleInformation(sampleDefinition).increaseTimesInvoked();
    }
    public static void log(SampleDefinition sampleDefinition, MethodCall actualMethodCall) {
        getSampleInformation(sampleDefinition).addMethodCall(actualMethodCall);
    }

    private static SampleExecutionInformation getSampleInformation(SampleDefinition sampleDefinition) {
        ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(sampleDefinition.getSampledMethod().getTarget());

        return executionInformation.getOrCreateBySample(sampleDefinition);
    }

}
