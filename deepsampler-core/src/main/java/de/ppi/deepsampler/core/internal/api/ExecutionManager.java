/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.internal.api;

import de.ppi.deepsampler.core.api.SampleReturnProcessor;
import de.ppi.deepsampler.core.model.*;

import java.util.ArrayList;
import java.util.List;

public class ExecutionManager {

    private ExecutionManager() {
        // This constructor is private since this class is not intended to be instantiated.
    }

    public static void recordMethodCall(final SampleDefinition sampleDefinition, final MethodCall actualMethodCall) {
        getSampleExecutionInformation(sampleDefinition).addMethodCall(actualMethodCall);
    }

    private static SampleExecutionInformation getSampleExecutionInformation(final SampleDefinition sampleDefinition) {
        final var executionInformation = ExecutionRepository.getInstance().getOrCreate(sampleDefinition.getSampledMethod().getTarget());

        return executionInformation.getOrCreateBySample(sampleDefinition);
    }

    public static Object execute(final SampleDefinition sampleDefinition, final StubMethodInvocation stubMethodInvocation) throws Throwable {
        Object callReturnValue = null;
        try {
            callReturnValue = sampleDefinition.getAnswer().call(stubMethodInvocation);
        } finally {
            for (var sampleReturnProcessor : getApplicableReturnProcessors(sampleDefinition)) {
                callReturnValue = sampleReturnProcessor.onReturn(sampleDefinition, stubMethodInvocation, callReturnValue);
            }
        }
        return callReturnValue;
    }

    private static List<SampleReturnProcessor> getApplicableReturnProcessors(final SampleDefinition sampleDefinition) {
        final var allSampleReturnProcessors = new ArrayList<SampleReturnProcessor>();

        final var globalSampleReturnProcessors = ExecutionRepository.getInstance().getGlobalProcessors();
        final var localSampleReturnProcessors = ExecutionRepository.getInstance().getSampleReturnProcessorsFor(sampleDefinition);

        allSampleReturnProcessors.addAll(globalSampleReturnProcessors);
        allSampleReturnProcessors.addAll(localSampleReturnProcessors);

        return allSampleReturnProcessors;
    }

}
