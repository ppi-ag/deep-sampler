/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit.json;

import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SampledMethod;
import de.ppi.deepsampler.junit.SampleRootPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static de.ppi.deepsampler.junit.AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JsonSourceManagerFactoryTest {

    @Mock
    private LoadSamples mockedLoadSamples;


    @Test
    void unmatchedSerializerTypesAreDetected() throws NoSuchMethodException {
        // GIVEN
        final Method errorProneMethod = Example.class.getMethod("wrongSerializerConfig");
        final JsonSourceManagerFactory jsonSourceManagerFactory = new JsonSourceManagerFactory();

        // THEN
        final InvalidConfigException actualException = assertThrows(InvalidConfigException.class, () -> jsonSourceManagerFactory.createSourceManagerToLoadSamples(errorProneMethod));
        assertEquals("de.ppi.deepsampler.junit.json.MyInstantSerializer must have a parameter of type java.util.Date " +
                        "since the serializer is registered for the latter type. But it is java.time.Instant",
                actualException.getMessage());

    }

    @Test
    void unmatchedDeserializerTypesAreDetected() throws NoSuchMethodException {
        // GIVEN
        final Method errorProneMethod = Example.class.getMethod("wrongDeserializerConfig");
        final JsonSourceManagerFactory jsonSourceManagerFactory = new JsonSourceManagerFactory();

        // THEN
        final InvalidConfigException actualException = assertThrows(InvalidConfigException.class, () -> jsonSourceManagerFactory.createSourceManagerToLoadSamples(errorProneMethod));
        assertEquals("de.ppi.deepsampler.junit.json.MyInstantDeserializer must have a parameter of type java.util.Date " +
                        "since the deserializer is registered for the latter type. But it is java.time.Instant",
                actualException.getMessage());

    }

    @Test
    void innerClassAsSerializerCanBeUsed() throws NoSuchMethodException {
        // GIVEN
        final Method methodWithInnerClassSerializer = Example.class.getMethod("loadInnerSerializerClass");
        final JsonSourceManagerFactory jsonSourceManagerFactory = new JsonSourceManagerFactory();

        final SampledMethod sampledMethod = new SampledMethod(TestService.class, TestService.class.getMethod("getInstant"));
        final SampleDefinition sampleDefinition = new SampleDefinition(sampledMethod);
        sampleDefinition.setMarkedForPersistence(true);
        SampleRepository.getInstance().add(sampleDefinition);

        // THEN
        assertDoesNotThrow(() -> jsonSourceManagerFactory.createSourceManagerToLoadSamples(methodWithInnerClassSerializer));
    }

    @Test
    void completeDefaultPathForFilesystemCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        final Method testMethod = Example.class.getMethod("loadInnerSerializerClass");
        final LoadSamples loadSamples = testMethod.getAnnotation(LoadSamples.class);
        final JsonSourceManagerFactory jsonSourceManagerFactory = new JsonSourceManagerFactory();

        // WHEN
        final Path defaultPath = jsonSourceManagerFactory.createPathForFilesystem(Optional.empty(), DEFAULT_VALUE_MUST_BE_CALCULATED, testMethod);

        // THEN
        final Path expectedDefaultPath = Paths.get("./", "de/ppi/deepsampler/junit/json", "Example_loadInnerSerializerClass.json");
        assertEquals(expectedDefaultPath, defaultPath);
    }

    @Test
    void completeDefaultPathForClasspathCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        final Method testMethod = Example.class.getMethod("loadInnerSerializerClass");
        final JsonSourceManagerFactory jsonSourceManagerFactory = new JsonSourceManagerFactory();

        // WHEN
        when(mockedLoadSamples.value()).thenReturn(DEFAULT_VALUE_MUST_BE_CALCULATED);

        final String defaultPath = jsonSourceManagerFactory.createPathForClasspath(mockedLoadSamples, testMethod);

        // THEN
        final String expectedDefaultPath = "/de/ppi/deepsampler/junit/json/Example_loadInnerSerializerClass.json";
        assertEquals(expectedDefaultPath, defaultPath);
    }

    @Test
    void pathForFileSystemWithRootCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        final Method testMethod = Example.class.getMethod("loadInnerSerializerClass");
        final SampleRootPath sampleRootPath = Example.class.getAnnotation(SampleRootPath.class);
        final JsonSourceManagerFactory jsonSourceManagerFactory = new JsonSourceManagerFactory();

        // WHEN
        final Path defaultPath = jsonSourceManagerFactory.createPathForFilesystem(Optional.of(sampleRootPath), DEFAULT_VALUE_MUST_BE_CALCULATED, testMethod);

        // THEN
        final String expectedFileName = "./src/test/resources/de/ppi/deepsampler/junit/json/Example_loadInnerSerializerClass.json".replace("/", File.separator);
        assertEquals(expectedFileName, defaultPath.toString());
    }

    @Test
    void pathForFileSystemWithCustomFilenameCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        final Method testMethod = Example.class.getMethod("loadInnerSerializerClass");
        final SampleRootPath sampleRootPath = Example.class.getAnnotation(SampleRootPath.class);
        final JsonSourceManagerFactory jsonSourceManagerFactory = new JsonSourceManagerFactory();

        // WHEN
        final Path path = jsonSourceManagerFactory.createPathForFilesystem(Optional.of(sampleRootPath), "myCustom.file", testMethod);

        // THEN
        final String expectedFileName = "./src/test/resources/myCustom.file".replace("/", File.separator);
        assertEquals(expectedFileName, path.toString());
    }

    @Test
    void pathForClasspathWithCustomFilenameCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        final Method testMethod = Example.class.getMethod("loadInnerSerializerClass");
        final JsonSourceManagerFactory jsonSourceManagerFactory = new JsonSourceManagerFactory();

        // WHEN
        when(mockedLoadSamples.value()).thenReturn("myCustomFile.json");

        final String defaultPath = jsonSourceManagerFactory.createPathForClasspath(mockedLoadSamples, testMethod);

        // THEN
        final String expectedDefaultPath = "myCustomFile.json";
        assertEquals(expectedDefaultPath, defaultPath);
    }



    @SampleRootPath("./src/test/resources/")
    public class Example {

        @LoadSamples
        @UseJsonSerializer(forType = Date.class, serializer = MyInstantSerializer.class)
        public void wrongSerializerConfig() {
            // nothing to do here because only the reaction to the annotations is tested.
        }

        @LoadSamples
        @UseJsonDeserializer(forType = Date.class, deserializer = MyInstantDeserializer.class)
        public void wrongDeserializerConfig() {
            // nothing to do here because only the reaction to the annotations is tested.
        }

        @LoadSamples
        @UseJsonDeserializer(forType = Instant.class, deserializer = MyInnerDateSerializer.class)
        public void loadInnerSerializerClass() {
            // nothing to do here because only the reaction to the annotations is tested.
        }

        public class MyInnerDateSerializer extends MyInstantDeserializer {
            // nothing to do here, because we simply use the inherited logic.
        }
    }
}