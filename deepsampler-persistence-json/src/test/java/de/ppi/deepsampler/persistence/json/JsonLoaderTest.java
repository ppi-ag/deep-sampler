/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import de.ppi.deepsampler.core.model.ExecutionRepository;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.persistence.model.PersistentModel;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentSampleMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonLoaderTest {

    @Test
    void testLoad() {
        // GIVEN
        final Path path = Paths.get("./record/testPersistent.json");

        // WHEN
        final PersistentModel persistentModel = new JsonLoader(new PersistentFile(path)).load();

        // THEN
        assertEquals(1, persistentModel.getSampleMethodToSampleMap().size());
        assertEquals(2, persistentModel.getSampleMethodToSampleMap().get(new JsonPersistentSampleMethod("TestMethodForRecord")).getAllCalls().size());
    }

    @Test
    void testLoadTime() {
        // GIVEN
        final Path path = Paths.get("./record/testTimePersistent.json");

        // WHEN
        final PersistentModel persistentModel = new JsonLoader(new PersistentFile(path)).load();

        // THEN
        assertEquals(1, persistentModel.getSampleMethodToSampleMap().size());
        assertEquals(2, persistentModel.getSampleMethodToSampleMap().get(new JsonPersistentSampleMethod("TestMethodForRecord")).getAllCalls().size());
    }

    @AfterEach
     void cleanUp() {
        ExecutionRepository.getInstance().clear();
        SampleRepository.getInstance().clear();
    }
}
