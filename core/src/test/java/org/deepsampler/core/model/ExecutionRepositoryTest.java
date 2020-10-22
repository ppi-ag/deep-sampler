package org.deepsampler.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionRepositoryTest {

    @Test
    void testGetOrCreate() {
        // WHEN
        final ExecutionInformation information = ExecutionRepository.getInstance().getOrCreate(getClass());

        // THEN
        assertTrue(information == ExecutionRepository.getInstance().getOrCreate(getClass()));
    }

}