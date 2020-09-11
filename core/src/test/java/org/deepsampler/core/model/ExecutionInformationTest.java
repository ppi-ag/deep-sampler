package org.deepsampler.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionInformationTest {

    @Test
    void testGetOrCreateBySample() throws NoSuchMethodException {
        // GIVEN
        ExecutionInformation executionInformation = new ExecutionInformation();
        SampleDefinition sampleDefinition = new SampleDefinition(new SampledMethod(getClass(), getClass().getMethod("toString")));

        // WHEN
        SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(sampleDefinition);

        // THEN
        assertTrue(sampleExecutionInformation == executionInformation.getOrCreateBySample(sampleDefinition));
    }
}