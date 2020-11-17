/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionInformation {

    private final Map<SampleDefinition, SampleExecutionInformation> behaviorExecutionInformationMap = new HashMap<>();

    public SampleExecutionInformation getOrCreateBySample(final SampleDefinition sampleDefinition) {
        return behaviorExecutionInformationMap.computeIfAbsent(sampleDefinition, b -> new SampleExecutionInformation(0));
    }

    public Map<SampleDefinition, SampleExecutionInformation> getAll() {
        return Collections.unmodifiableMap(behaviorExecutionInformationMap);
    }
}
