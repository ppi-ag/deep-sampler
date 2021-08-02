package de.ppi.deepsampler.provider.common;

import org.junit.jupiter.api.extension.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A small convenience-extension, that creates a name for a json-file depending on the name of a test-method, and also deletes the file after
 * the test has completed. The files are stored in the folder ./record.
 */
public class TempJsonFile implements ParameterResolver, BeforeEachCallback, AfterEachCallback {

    private Path tempFile;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(Path.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return tempFile;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        Files.deleteIfExists(tempFile);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        tempFile = Paths.get("./record", extensionContext.getDisplayName() + ".json");
    }
}
