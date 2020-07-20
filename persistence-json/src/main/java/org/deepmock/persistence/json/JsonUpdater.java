package org.deepmock.persistence.json;

import java.nio.file.Path;

public class JsonUpdater {
    private final Path path;

    public JsonUpdater(Path path) {
        this.path = path;
    }
}
