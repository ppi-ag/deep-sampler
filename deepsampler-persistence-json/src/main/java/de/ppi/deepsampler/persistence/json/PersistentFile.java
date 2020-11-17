package de.ppi.deepsampler.persistence.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * Representation of an ordinary file in the file system.
 */
public class PersistentFile implements PersistentResource {

    private final Path filePath;

    public PersistentFile(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * @return path of the persistent file in the filesystem
     */
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public InputStream readAsStream(OpenOption... openOptions) throws IOException {
        return Files.newInputStream(filePath, openOptions);
    }
    @Override
    public OutputStream writeAsStream(OpenOption... openOptions) throws IOException {
        return Files.newOutputStream(filePath, openOptions);
    }
}
