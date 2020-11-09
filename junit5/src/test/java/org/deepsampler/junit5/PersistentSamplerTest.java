package org.deepsampler.junit5;

import org.deepsampler.core.api.Sample;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.core.model.SampledMethod;
import org.deepsampler.junit.*;
import org.deepsampler.persistence.json.JsonSourceManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DeepSamplerExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PersistentSamplerTest {

    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get("org", "deepsampler", "junit5", "PersistentSamplerTest_samplerCanBeSaved.json");
    public static final String SAVED_IN_SPECIFIC_FILE_JSON = "org/deepsampler/junit5/samplerCanBeSavedInSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_JSON = "src/test/resources/org/deepsampler/junit5/samplerCanBeLoadedFromSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_FROM_CLASSPATH_JSON = "samplerCanBeLoadedFromSpecificFile.json";

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples
    @Order(0)
    void samplerCanBeLoadedFromFile() throws Exception {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        assertFalse(sampleRepository.isEmpty());

        final SampledMethod expectedSampledMethod = new SampledMethod(TestBean.class, TestBean.class.getMethod("getSomeInt"));
        final SampleDefinition getSomeInt = sampleRepository.findAllForMethod(expectedSampledMethod).get(0);

        assertEquals(42, getSomeInt.getAnswer().call(null));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples
    @Order(1)
    void samplerCanBeSaved() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        if (Files.exists(EXPECTED_SAVED_SAMPLER)) {
            Files.delete(EXPECTED_SAVED_SAMPLER);
        }

        assertFalse(Files.exists(EXPECTED_SAVED_SAMPLER));
    }



    @Test
    @Order(2)
    void samplerHasBeenSavedByPriorTestMethod() {
        assertTrue(Files.exists(EXPECTED_SAVED_SAMPLER));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(file = SAVED_IN_SPECIFIC_FILE_JSON)
    @Order(3)
    void samplerCanBeSavedInSpecificFile() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        if (Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON))) {
            Files.delete(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON));
        }

        assertFalse(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @Order(4)
    void samplerHasBeenSavedInSpecificFileByPriorTestMethod() {
        assertTrue(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(file = SAVED_IN_SPECIFIC_FILE_JSON, persistenceManagerProvider = TestPersistenceMangerProvider.class)
    @Order(5)
    void samplerCanBeSavedInSpecificFileWithSpecificBuilder() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        if (Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON))) {
            Files.delete(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON));
        }

        assertFalse(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @Order(6)
    void samplerHasBeenSavedInSpecificWithSpecificBuilderFileByPriorTestMethod() {
        assertTrue(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(file = LOAD_SPECIFIC_FILE_JSON)
    @Order(7)
    public void samplerCanBeLoadedFromSpecificFile() throws Exception {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        assertFalse(sampleRepository.isEmpty());

        final SampledMethod expectedSampledMethod = new SampledMethod(TestBean.class, TestBean.class.getMethod("getSomeInt"));
        final SampleDefinition getSomeInt = sampleRepository.findAllForMethod(expectedSampledMethod).get(0);

        assertEquals(42, getSomeInt.getAnswer().call(null));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(classPath = LOAD_SPECIFIC_FILE_FROM_CLASSPATH_JSON)
    @Order(8)
    public void samplerCanBeLoadedFromSpecificClasspathResource() throws Exception {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        assertFalse(sampleRepository.isEmpty());

        final SampledMethod expectedSampledMethod = new SampledMethod(TestBean.class, TestBean.class.getMethod("getSomeInt"));
        final SampleDefinition getSomeInt = sampleRepository.findAllForMethod(expectedSampledMethod).get(0);

        assertEquals(42, getSomeInt.getAnswer().call(null));
    }


    public static class TestSampleFixture implements SamplerFixture {

        @PrepareSampler
        private TestBean testBeanSampler;

        @Override
        public void defineSamplers() {
            Sample.of(testBeanSampler.getSomeInt());
        }
    }

    public static class TestPersistenceMangerProvider implements PersistentSampleManagerProvider {

        @Override
        public JsonSourceManager.Builder configurePersistentSampleManager() {
            return JsonSourceManager.builder();
        }
    }

}
