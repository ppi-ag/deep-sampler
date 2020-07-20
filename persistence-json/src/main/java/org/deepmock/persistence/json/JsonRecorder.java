package org.deepmock.persistence.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.deepmock.core.model.Behavior;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonRecorder {
    private final Path path;

    public JsonRecorder(Path path) {
        this.path = path;
    }

    public void record(List<Behavior> behaviors) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(path.toFile(), toPersistentModel(behaviors));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonPersonalityModel toPersistentModel(List<Behavior> behaviors) {
        return new JsonPersonalityModel(UUID.randomUUID().toString(), behaviors.stream()
                .map(bhv -> toPersistentBehavior(bhv))
                .collect(Collectors.toList()));
    }

    private JsonPersistentBehavior toPersistentBehavior(Behavior bhv) {
        return new JsonPersistentBehavior();
    }
}
