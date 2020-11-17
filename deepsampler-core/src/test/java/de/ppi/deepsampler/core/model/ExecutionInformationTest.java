/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class ExecutionInformationTest {

    @Test
    void testGetOrCreateBySample() throws NoSuchMethodException {
        // GIVEN
        final ExecutionInformation executionInformation = new ExecutionInformation();
        final SampleDefinition sampleDefinition = new SampleDefinition(new SampledMethod(getClass(), getClass().getMethod("toString")));

        // WHEN
        final SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(sampleDefinition);

        // THEN
        assertSame(sampleExecutionInformation, executionInformation.getOrCreateBySample(sampleDefinition));
    }
}