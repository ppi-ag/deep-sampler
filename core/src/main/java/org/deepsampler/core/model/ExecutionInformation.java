package org.deepsampler.core.model;

import java.util.HashMap;
import java.util.Map;

public class ExecutionInformation {

    private final Map<SampleDefinition, SampleExecutionInformation> behaviorExecutionInformationMap = new HashMap<>();

    public SampleExecutionInformation getOrCreateBySample(SampleDefinition behavior) {
        return behaviorExecutionInformationMap.computeIfAbsent(behavior, b -> new SampleExecutionInformation(0));
    }

}
