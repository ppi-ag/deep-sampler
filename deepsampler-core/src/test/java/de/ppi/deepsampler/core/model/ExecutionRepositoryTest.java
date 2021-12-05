/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

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