package org.deepsampler.junit4;

import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.core.model.SampledMethod;
import org.deepsampler.junit.*;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistentSamplerTest {

    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get("org", "deepsampler", "junit4", "PersistentSamplerTest_bSamplerCanBeSaved.json");
    public static final String SAVED_IN_SPECIFIC_FILE_JSON = "org/deepsampler/junit4/samplerCanBeSavedInSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_JSON = "src/test/resources/org/deepsampler/junit4/fSamplerCanBeLoadedFromSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_FROM_CLASSPATH_JSON = "fSamplerCanBeLoadedFromSpecificFile.json";

    @Rule
    public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples
    public void aSamplerCanBeLoadedFromFile() throws Exception {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        assertFalse(sampleRepository.isEmpty());

        final SampledMethod expectedSampledMethod = new SampledMethod(TestBean.class, TestBean.class.getMethod("getSomeInt"));
        final SampleDefinition getSomeInt = sampleRepository.findAllForMethod(expectedSampledMethod).get(0);

        assertEquals(42, getSomeInt.getAnswer().call(null));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples
    public void bSamplerCanBeSaved() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        if (Files.exists(EXPECTED_SAVED_SAMPLER)) {
            Files.delete(EXPECTED_SAVED_SAMPLER);
        }

        assertFalse(Files.exists(EXPECTED_SAVED_SAMPLER));
    }

    @Test
    public void cTheSavedSamplerExists() {
        assertTrue(Files.exists(EXPECTED_SAVED_SAMPLER));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(file = SAVED_IN_SPECIFIC_FILE_JSON)
    public void dSamplerCanBeSavedInSpecificFile() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        if (Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON))) {
            Files.delete(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON));
        }

        assertFalse(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    public void eSamplerHasBeenSavedInSpecificFileByPriorTestMethod() {
        assertTrue(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(file = SAVED_IN_SPECIFIC_FILE_JSON, persistenceManagerProvider = TestPersistenceManagerProvider.class)
    public void fSamplerCanBeSavedInSpecificFileWithSpecificBuilder() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        if (Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON))) {
            Files.delete(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON));
        }

        assertFalse(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    public void gSamplerHasBeenSavedInSpecificWithSpecificBuilderFileByPriorTestMethod() {
        assertTrue(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(file = LOAD_SPECIFIC_FILE_JSON)
    public void fSamplerCanBeLoadedFromSpecificFile() throws Exception {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        assertFalse(sampleRepository.isEmpty());

        final SampledMethod expectedSampledMethod = new SampledMethod(TestBean.class, TestBean.class.getMethod("getSomeInt"));
        final SampleDefinition getSomeInt = sampleRepository.findAllForMethod(expectedSampledMethod).get(0);

        assertEquals(42, getSomeInt.getAnswer().call(null));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(classPath = LOAD_SPECIFIC_FILE_FROM_CLASSPATH_JSON)
    public void gSamplerCanBeLoadedFromSpecificClasspathResource() throws Exception {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        assertFalse(sampleRepository.isEmpty());

        final SampledMethod expectedSampledMethod = new SampledMethod(TestBean.class, TestBean.class.getMethod("getSomeInt"));
        final SampleDefinition getSomeInt = sampleRepository.findAllForMethod(expectedSampledMethod).get(0);

        assertEquals(42, getSomeInt.getAnswer().call(null));
    }


}
