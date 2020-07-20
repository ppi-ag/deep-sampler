package org.deepmock.persistence.json;

import org.deepmock.persistence.SourceManager;
import org.deepmock.core.model.Behavior;

import java.nio.file.Path;
import java.util.List;

public class JsonSourceManager implements SourceManager {

    private JsonRecorder jsonRecorder;
    private JsonUpdater jsonUpdater;
    private JsonLoader jsonLoader;

    public JsonSourceManager(Path path) {
        this.jsonLoader = new JsonLoader(path);
        this.jsonUpdater = new JsonUpdater(path);
        this.jsonRecorder = new JsonRecorder(path);
    }

    @Override
    public void record(List<Behavior> behaviors) {
        this.jsonRecorder.record(behaviors);
    }

    @Override
    public void update(List<Behavior> behaviors) {

    }

    @Override
    public void load(List<Behavior> behaviors) {

    }
}
