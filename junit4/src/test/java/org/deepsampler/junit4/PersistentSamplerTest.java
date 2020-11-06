package org.deepsampler.junit4;

import org.deepsampler.core.api.Sample;
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

    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get("org", "deepsampler", "junit4", "PersistentSamplerTest_aSamplerCanBeSaved.json");


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
    public void aSamplerCanBeSaved() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        if (Files.exists(EXPECTED_SAVED_SAMPLER)) {
            Files.delete(EXPECTED_SAVED_SAMPLER);
        }

        assertFalse(Files.exists(EXPECTED_SAVED_SAMPLER));
    }

    @Test
    public void theSavedSamplerExists() {
        assertTrue(Files.exists(EXPECTED_SAVED_SAMPLER));
    }

    public static class TestSampleFixture implements SamplerFixture {

        @PrepareSampler
        private TestBean testBeanSampler;

        @Override
        public void defineSamplers() {
            Sample.of(testBeanSampler.getSomeInt());
        }
    }

}
