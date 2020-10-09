package org.deepsampler.persistence.json;

import org.deepsampler.core.model.ExecutionRepository;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.persistence.json.model.JsonPersistentSampleMethod;
import org.deepsampler.persistence.json.model.PersistentModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonLoaderTest {

    @Test
    public void testLoad() {
        // GIVEN
        Path path = Paths.get("./record/testPersistent.json");

        // WHEN
        PersistentModel persistentModel = new JsonLoader(path).load();

        // THEN
        assertEquals(1, persistentModel.getSampleMethodToSampleMap().size());
        assertEquals(2, persistentModel.getSampleMethodToSampleMap().get(new JsonPersistentSampleMethod("TestMethodForRecord")).getAllCalls().size());
    }

    @Test
    public void testLoadTime() {
        // GIVEN
        Path path = Paths.get("./record/testTimePersistent.json");

        // WHEN
        PersistentModel persistentModel = new JsonLoader(path).load();

        // THEN
        assertEquals(1, persistentModel.getSampleMethodToSampleMap().size());
        assertEquals(2, persistentModel.getSampleMethodToSampleMap().get(new JsonPersistentSampleMethod("TestMethodForRecord")).getAllCalls().size());
    }

    @AfterEach
    public void cleanUp() {
        ExecutionRepository.getInstance().clear();
        SampleRepository.getInstance().clear();
    }
}
