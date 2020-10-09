package org.deepsampler.persistence.json;

import org.deepsampler.persistence.json.model.PersistentModel;
import org.deepsampler.core.model.ExecutionInformation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class JsonSourceManager implements SourceManager {

    private final JsonRecorder jsonRecorder;
    private final JsonLoader jsonLoader;

    public JsonSourceManager(String pathAsStr) {
        this(Paths.get(pathAsStr));
    }

    public JsonSourceManager(Path path) {
        this.jsonLoader = new JsonLoader(path);
        this.jsonRecorder = new JsonRecorder(path);
    }

    @Override
    public void record(Map<Class<?>, ExecutionInformation> executionInformation) {
        jsonRecorder.record(executionInformation);
    }

    @Override
    public PersistentModel load() {
        return jsonLoader.load();
    }
}
