package org.deepsampler.core.internal.api;

import org.deepsampler.core.model.*;

public class ExecutionManager {

    public static void notify(final SampleDefinition sampleDefinition) {
        getSampleInformation(sampleDefinition).increaseTimesInvoked();
    }

    public static void record(final SampleDefinition sampleDefinition, final MethodCall actualMethodCall) {
        getSampleInformation(sampleDefinition).addMethodCall(actualMethodCall);
    }

    private static SampleExecutionInformation getSampleInformation(final SampleDefinition sampleDefinition) {
        final ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(sampleDefinition.getSampledMethod().getTarget());

        return executionInformation.getOrCreateBySample(sampleDefinition);
    }

}
