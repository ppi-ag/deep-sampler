/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.util.*;

/**
 * Keeps track of calls to stubbed methods by collecting {@link SampleExecutionInformation}s. This is used by the
 * verify-functionalities.
 */
public class ExecutionInformation {


    private final List<SampleExecutionInformation> sampleExecutionInformationList = new ArrayList<>();

    /**
     * If sampleDefinition is already known, the respective {@link SampleExecutionInformation} will be returned.
     *
     * Notice: Whether a {@link SampleDefinition} is already known, or not,  is checked by comparing the object identity (==)
     * of sampleDefinition, not using {@link SampleDefinition#equals(Object)}, because the equals() cannot compare the {@link ParameterMatcher}s of
     * a SampleDefinition. But this is necessary to discriminate between calls of a method with different parameters that would match
     * to different {@link ParameterMatcher}s.
     *
     * If sampleDefinition is not known yet, a new {@link SampleExecutionInformation} is created, registered and returned.
     *
     * @param sampleDefinition
     * @return A {@link SampleExecutionInformation} for sampleDefinition. A new one if non was known yet, or an already registered version.
     */
    public SampleExecutionInformation getOrCreateBySample(final SampleDefinition sampleDefinition) {
        for (SampleExecutionInformation execution : sampleExecutionInformationList) {
            if (execution.getSampleDefinition() == sampleDefinition) {
                return execution;
            }
        }

        SampleExecutionInformation execution = new SampleExecutionInformation(sampleDefinition);
        this.sampleExecutionInformationList.add(execution);
        return execution;
    }

    public List<SampleExecutionInformation> getAll() {
        return sampleExecutionInformationList;
    }
}
