package org.deepsampler.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionInformationTest {

    @Test
    void testGetOrCreateBySample() throws NoSuchMethodException {
        // GIVEN
        final ExecutionInformation executionInformation = new ExecutionInformation();
        final SampleDefinition sampleDefinition = new SampleDefinition(new SampledMethod(getClass(), getClass().getMethod("toString")));

        // WHEN
        final SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(sampleDefinition);

        // THEN
        assertTrue(sampleExecutionInformation == executionInformation.getOrCreateBySample(sampleDefinition));
    }
}