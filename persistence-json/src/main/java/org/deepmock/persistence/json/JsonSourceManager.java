package org.deepmock.persistence.json;

import org.deepmock.core.model.ExecutionInformation;
import org.deepmock.persistence.SourceManager;
import org.deepmock.core.model.Behavior;

import java.nio.file.Path;
import java.util.List;
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
    public void load() {

    }
}
