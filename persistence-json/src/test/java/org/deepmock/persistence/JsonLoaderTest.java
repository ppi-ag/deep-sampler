package org.deepmock.persistence;

import org.deepmock.persistence.json.JsonLoader;
import org.deepmock.persistence.json.model.JsonPersistentJoinPoint;
import org.deepmock.persistence.model.PersistentModel;
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
        assertEquals(2, persistentModel.getJoinPointBehaviorMap().get(new JsonPersistentJoinPoint("TestMethodForRecord")).getAllCalls().size());
    }

    @Test
    public void testLoadTime() {
        // GIVEN
        Path path = Paths.get("./record/testTimePersistent.json");

        // WHEN
        PersistentModel persistentModel = new JsonLoader(path).load();

        // THEN
        assertEquals(1, persistentModel.getJoinPointBehaviorMap().size());
        assertEquals(2, persistentModel.getJoinPointBehaviorMap().get(new JsonPersistentJoinPoint("TestMethodForRecord")).getAllCalls().size());
    }
}
