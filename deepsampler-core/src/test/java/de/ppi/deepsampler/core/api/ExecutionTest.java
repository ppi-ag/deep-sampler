/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.model.ExecutionRepository;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionTest {

    @Test
    void useGlobal() {
        // GIVEN
        SampleReturnProcessor sampleReturnProcessor = (a, b, c) -> null;

        // WHEN
        Execution.useGlobal(sampleReturnProcessor);

        // THEN
        assertEquals(sampleReturnProcessor, ExecutionRepository.getInstance().getGlobalProcessors().get(0));
    }

    @Test
    void useForLastSample() {
        // GIVEN
        final SampleDefinition sdSampler = Sampler.prepare(SampleDefinition.class);
        Sample.of(sdSampler.getSampleId()).is("");
        final SampleReturnProcessor sampleReturnProcessor = (a, b, c) -> null;

        // WHEN
        Execution.useForLastSample(sampleReturnProcessor);

        // THEN
        assertEquals(sampleReturnProcessor, ExecutionRepository.getInstance().getSampleReturnProcessorsFor(SampleRepository.getInstance().getLastSampleDefinition()).get(0));
    }

    @Test
    void testNotSupported() {
        // GIVEN WHEN THEN
        assertThrows(InvalidConfigException.class, () -> Execution.setScope(null));
    }
}