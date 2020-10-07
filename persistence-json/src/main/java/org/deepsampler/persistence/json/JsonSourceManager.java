package org.deepsampler.persistence.json;

import org.deepsampler.persistence.SourceManager;
import org.deepsampler.persistence.model.PersistentModel;
import org.deepsampler.core.model.ExecutionInformation;

import java.nio.file.Path;
import java.util.Map;

public class JsonSourceManager implements SourceManager {

    private JsonRecorder jsonRecorder;
    private JsonLoader jsonLoader;

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
