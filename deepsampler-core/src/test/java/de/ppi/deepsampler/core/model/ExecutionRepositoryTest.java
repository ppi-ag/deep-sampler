package de.ppi.deepsampler.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class ExecutionRepositoryTest {

    @Test
    void testGetOrCreate() {
        // WHEN
        final ExecutionInformation information = ExecutionRepository.getInstance().getOrCreate(getClass());

        // THEN
        assertSame(information, ExecutionRepository.getInstance().getOrCreate(getClass()));
    }

}