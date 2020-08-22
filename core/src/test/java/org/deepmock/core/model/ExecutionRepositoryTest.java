package org.deepmock.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionRepositoryTest {

    @Test
    void testGetOrCreate() {
        // WHEN
        ExecutionInformation information = ExecutionRepository.getInstance().getOrCreate(getClass());

        // THEN
        assertTrue(information == ExecutionRepository.getInstance().getOrCreate(getClass()));
    }

}