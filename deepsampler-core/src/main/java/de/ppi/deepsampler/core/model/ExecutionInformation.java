/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.util.*;

public class ExecutionInformation {

    private final List<SampleExecutionInformation> executionInformation = new ArrayList<>();

    public SampleExecutionInformation getOrCreateBySample(final SampleDefinition sampleDefinition) {
        for (SampleExecutionInformation execution : executionInformation) {
            if (execution.getSampleDefinition() == sampleDefinition) {
                return execution;
            }
        }

        SampleExecutionInformation execution = new SampleExecutionInformation(sampleDefinition);
        this.executionInformation.add(execution);
        return execution;
    }

    public List<SampleExecutionInformation> getAll() {
        return executionInformation;
    }
}
