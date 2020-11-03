package org.deepsampler.persistence;

import org.deepsampler.persistence.error.JsonPersistenceException;
import org.deepsampler.persistence.model.JsonSampleModel;
import org.deepsampler.persistence.model.PersistentModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonLoader extends JsonOperator {
    private final Path path;

    public JsonLoader(final Path path) {
        this.path = path;
    }

    public PersistentModel load() {
        try {
            return createObjectMapper().readValue(Files.newBufferedReader(path), JsonSampleModel.class);
        } catch (final IOException e) {
            throw new JsonPersistenceException("It was not possible to deserialize/read the file", e);
        }
    }
}
