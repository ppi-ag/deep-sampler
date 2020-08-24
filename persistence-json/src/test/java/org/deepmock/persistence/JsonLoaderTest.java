package org.deepmock.persistence;

import org.deepmock.core.model.Behavior;
import org.deepmock.persistence.json.JsonLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonLoaderTest {

    @Test
    public void testLoad() {
        // GIVEN
        Path path = Paths.get("./record/testPersistent.json");

        // WHEN
        List<Behavior> behaviorList = new JsonLoader(path).load();

        // THEN
        assertEquals(2, behaviorList.size());
    }
}
