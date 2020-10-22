package org.deepsampler.persistence.json;

import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.persistence.json.model.PersistentModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class JsonSourceManager implements SourceManager {

    private final JsonRecorder jsonRecorder;
    private final JsonLoader jsonLoader;

    public JsonSourceManager(final String pathAsStr) {
        this(Paths.get(pathAsStr));
    }

    public JsonSourceManager(final Path path) {
        this.jsonLoader = new JsonLoader(path);
        this.jsonRecorder = new JsonRecorder(path);
    }

    @Override
    public void record(final Map<Class<?>, ExecutionInformation> executionInformation) {
        jsonRecorder.record(executionInformation);
    }

    @Override
    public PersistentModel load() {
        return jsonLoader.load();
    }
}
