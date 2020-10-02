package org.deepmock.persistence.json;

import org.deepmock.persistence.json.error.JsonPersistenceException;
import org.deepmock.persistence.json.model.JsonPersonalityModel;
import org.deepmock.persistence.model.PersistentModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonLoader extends JsonOperator {
    private final Path path;

    public JsonLoader(Path path) {
        this.path = path;
    }

    public PersistentModel load() {
        try {
            return createObjectMapper().readValue(Files.newBufferedReader(path), JsonPersonalityModel.class);
        } catch (IOException e) {
            throw new JsonPersistenceException("It was not possible to deserialize/read the file", e);
        }
    }
}
