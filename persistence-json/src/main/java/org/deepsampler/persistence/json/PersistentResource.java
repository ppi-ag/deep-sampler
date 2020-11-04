package org.deepsampler.persistence.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.OpenOption;

public interface PersistentResource {
    InputStream readAsStream(OpenOption... openOptions) throws IOException;
    OutputStream writeAsStream(OpenOption... openOptions) throws IOException;
}
