package org.deepsampler.junit5;

import org.deepsampler.core.api.Sample;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.core.model.SampledMethod;
import org.deepsampler.junit.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DeepSamplerExtension.class)
public class PersistentSamplerTest {

    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get("org", "deepsampler", "junit5", "PersistentSamplerTest_samplerCanBeSaved.json");

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

    public static class TestSampleFixture implements SamplerFixture {

        @PrepareSampler
        private TestBean testBeanSampler;

        @Override
        public void defineSamplers() {
            Sample.of(testBeanSampler.getSomeInt());
        }
    }

}
