package org.deepsampler.persistence;

import org.deepsampler.persistence.json.JsonLoader;
import org.deepsampler.persistence.json.model.JsonPersistentSampleMethod;
import org.deepsampler.persistence.model.PersistentModel;
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
        assertEquals(1, persistentModel.getJoinPointBehaviorMap().size());
        assertEquals(2, persistentModel.getJoinPointBehaviorMap().get(new JsonPersistentSampleMethod("TestMethodForRecord")).getAllCalls().size());
    }

    @Test
    public void testLoadTime() {
        // GIVEN
        Path path = Paths.get("./record/testTimePersistent.json");

        // WHEN
        PersistentModel persistentModel = new JsonLoader(path).load();

        // THEN
        assertEquals(1, persistentModel.getJoinPointBehaviorMap().size());
        assertEquals(2, persistentModel.getJoinPointBehaviorMap().get(new JsonPersistentSampleMethod("TestMethodForRecord")).getAllCalls().size());
    }
}
