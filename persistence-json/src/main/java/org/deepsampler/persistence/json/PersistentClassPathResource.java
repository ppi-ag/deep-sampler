package org.deepsampler.persistence.json;

import org.deepsampler.persistence.json.error.JsonPersistenceException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;

public class PersistentClassPathResource implements PersistentResource {

    private final String resourcePath;
    private final Class<?> anchor;

    public PersistentClassPathResource(String resourcePath, Class<?> anchor) {
        this.resourcePath = resourcePath;
        this.anchor = anchor;
    }

    @Override
    public InputStream readAsStream(OpenOption... openOptions) {
        final InputStream resourceAsStream = anchor.getResourceAsStream(resourcePath);
        if (resourceAsStream == null) {
            throw new JsonPersistenceException("There is no resource %s.", resourcePath);
        }

        return resourceAsStream;
    }

    @Override
    public OutputStream writeAsStream(OpenOption... openOptions) {
        throw new IllegalArgumentException("You can't write java resources!");
    }
}
