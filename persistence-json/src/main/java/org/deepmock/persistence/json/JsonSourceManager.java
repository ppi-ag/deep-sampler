package org.deepmock.persistence.json;

import org.deepmock.core.model.ExecutionInformation;
import org.deepmock.persistence.SourceManager;
import org.deepmock.persistence.model.PersistentModel;

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
