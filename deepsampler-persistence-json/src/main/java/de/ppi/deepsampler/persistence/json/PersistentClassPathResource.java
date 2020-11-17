/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import de.ppi.deepsampler.persistence.json.error.JsonPersistenceException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;

/**
 * Representation of a file in the jar archive.
 */
public class PersistentClassPathResource implements PersistentResource {

    private final String resourcePath;
    private final Class<?> anchor;

    public PersistentClassPathResource(final String resourcePath, final Class<?> anchor) {
        this.resourcePath = resourcePath;
        this.anchor = anchor;
    }

    @Override
    public InputStream readAsStream(final OpenOption... openOptions) {
        final InputStream resourceAsStream = anchor.getResourceAsStream(resourcePath);
        if (resourceAsStream == null) {
            throw new JsonPersistenceException("There is no resource '%s'. It is searched on the classpath relative to the class %s", resourcePath, anchor.getName());
        }

        return resourceAsStream;
    }

    @Override
    public OutputStream writeAsStream(final OpenOption... openOptions) {
        throw new IllegalArgumentException("You can't write java resources!");
    }
}
