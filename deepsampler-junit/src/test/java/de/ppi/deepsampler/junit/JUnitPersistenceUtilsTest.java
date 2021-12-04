package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SampledMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import static de.ppi.deepsampler.junit.AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED;

class JUnitPersistenceUtilsTest {

    @Mock
    private LoadSamples mockedLoadSamples;

    @BeforeEach
    public void initMockito() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void unmatchedSerializerTypesAreDetected() throws NoSuchMethodException {
        // GIVEN
        Method errorProneMethod = Example.class.getMethod("wrongSerializerConfig");

        // THEN
        InvalidConfigException actualException = assertThrows(InvalidConfigException.class, () -> JUnitPersistenceUtils.loadSamples(errorProneMethod));
        assertEquals("de.ppi.deepsampler.junit.MyInstantSerializer must have a parameter type of type java.util.Date " +
                        "since the serializer is registered for the latter type. But it is java.time.Instant",
                actualException.getMessage());

    }

    @Test
    void unmatchedDeserializerTypesAreDetected() throws NoSuchMethodException {
        // GIVEN
        Method errorProneMethod = Example.class.getMethod("wrongDeserializerConfig");

        // THEN
        InvalidConfigException actualException = assertThrows(InvalidConfigException.class, () -> JUnitPersistenceUtils.loadSamples(errorProneMethod));
        assertEquals("de.ppi.deepsampler.junit.MyInstantDeserializer must have a parameter type of type java.util.Date " +
                        "since the deserializer is registered for the latter type. But it is java.time.Instant",
                actualException.getMessage());

    }

    @Test
    void innerClassAsSerializerCanBeUsed() throws NoSuchMethodException {
        // GIVEN
        Method methodWithInnerClassSerializer = Example.class.getMethod("loadInnerSerializerClass");

        SampledMethod sampledMethod = new SampledMethod(TestService.class, TestService.class.getMethod("getInstant"));
        SampleDefinition sampleDefinition = new SampleDefinition(sampledMethod);
        sampleDefinition.setMarkedForPersistence(true);
        SampleRepository.getInstance().add(sampleDefinition);

        // THEN
        assertDoesNotThrow(() -> JUnitPersistenceUtils.loadSamples(methodWithInnerClassSerializer));
    }

    @Test
    void completeDefaultPathForFilesystemCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        Method testMethod = Example.class.getMethod("loadInnerSerializerClass");
        LoadSamples loadSamples = testMethod.getAnnotation(LoadSamples.class);

        // WHEN
        Path defaultPath = JUnitPersistenceUtils.createPathForFilesystem(Optional.empty(), DEFAULT_VALUE_MUST_BE_CALCULATED, testMethod);

        // THEN
        Path expectedDefaultPath = Paths.get("./", "de/ppi/deepsampler/junit", "Example_loadInnerSerializerClass.json");
        assertEquals(expectedDefaultPath, defaultPath);
    }

    @Test
    void completeDefaultPathForClasspathCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        Method testMethod = Example.class.getMethod("loadInnerSerializerClass");

        // WHEN
        when(mockedLoadSamples.value()).thenReturn(DEFAULT_VALUE_MUST_BE_CALCULATED);

        String defaultPath = JUnitPersistenceUtils.createPathForClasspath(mockedLoadSamples, testMethod);

        // THEN
        String expectedDefaultPath = "/de/ppi/deepsampler/junit/Example_loadInnerSerializerClass.json";
        assertEquals(expectedDefaultPath, defaultPath);
    }

    @Test
    void pathForFileSystemWithRootCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        Method testMethod = Example.class.getMethod("loadInnerSerializerClass");
        SampleRootPath sampleRootPath = Example.class.getAnnotation(SampleRootPath.class);

        // WHEN
        Path defaultPath = JUnitPersistenceUtils.createPathForFilesystem(Optional.of(sampleRootPath), DEFAULT_VALUE_MUST_BE_CALCULATED, testMethod);

        // THEN
        String expectedFileName = "./myRoot/de/ppi/deepsampler/junit/Example_loadInnerSerializerClass.json".replace("/", File.separator);
        assertEquals(expectedFileName, defaultPath.toString());
    }

    @Test
    void pathForFileSystemWithCustomFilenameCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        Method testMethod = Example.class.getMethod("loadInnerSerializerClass");
        SampleRootPath sampleRootPath = Example.class.getAnnotation(SampleRootPath.class);

        // WHEN
        Path path = JUnitPersistenceUtils.createPathForFilesystem(Optional.of(sampleRootPath), "myCustom.file", testMethod);

        // THEN
        String expectedFileName = "./myRoot/myCustom.file".replace("/", File.separator);
        assertEquals(expectedFileName, path.toString());
    }

    @Test
    void pathForClasspathWithCustomFilenameCanBeCreated() throws NoSuchMethodException {
        // GIVEN
        Method testMethod = Example.class.getMethod("loadInnerSerializerClass");

        // WHEN
        when(mockedLoadSamples.value()).thenReturn("myCustomFile.json");

        String defaultPath = JUnitPersistenceUtils.createPathForClasspath(mockedLoadSamples, testMethod);

        // THEN
        String expectedDefaultPath = "myCustomFile.json";
        assertEquals(expectedDefaultPath, defaultPath);
    }





    @SampleRootPath("./myRoot")
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
