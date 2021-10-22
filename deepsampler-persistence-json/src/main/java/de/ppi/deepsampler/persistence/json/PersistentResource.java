/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;

/**
 * Interface for persistent resources, which are supported to be used with the {@link JsonSourceManager}.
 */
public interface PersistentResource {
    /**
     * Read the resource as inputStream. You can provide {@link OpenOption}s. They may be ignored depending on
     * the implementation.
     *
     * @param openOptions the open options
     * @return InputStream
     * @throws IOException exception if it is not possible to read the resource
     */
    InputStream readAsStream(OpenOption... openOptions) throws IOException;

    /**
     * Write the resource as stream. Will open a outputStream to make writing operations possible. May
     * not be implemented depending on the implementation.
     *
     * @param openOptions the open options
     * @return OutputStream
     * @throws IOException exception if it is not possible to read the resource
     */
    OutputStream writeAsStream(OpenOption... openOptions) throws IOException;
}
