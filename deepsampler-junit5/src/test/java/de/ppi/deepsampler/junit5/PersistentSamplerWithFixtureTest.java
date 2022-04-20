/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;

import com.google.inject.Guice;
import de.ppi.deepsampler.core.api.PersistentSample;
import de.ppi.deepsampler.junit.Cat;
import de.ppi.deepsampler.junit.PrepareSampler;
import de.ppi.deepsampler.junit.SampleRootPath;
import de.ppi.deepsampler.junit.SamplerFixture;
import de.ppi.deepsampler.junit.TestModule;
import de.ppi.deepsampler.junit.TestService;
import de.ppi.deepsampler.junit.UseSamplerFixture;
import de.ppi.deepsampler.junit.json.LoadSamples;
import de.ppi.deepsampler.junit.json.SaveSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DeepSamplerExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@UseSamplerFixture(PersistentSamplerWithFixtureTest.TestSamplerFixture.class)
class PersistentSamplerWithFixtureTest {

    private static final Path EXPECTED_SAMPLE_FILE = Paths.get("./src/test/tmp/de/ppi/deepsampler/junit5/PersistentSamplerWithFixtureTest_whenSavedWithRootPathOnFixture.json");

    @Inject
    private TestService testService;

    @BeforeEach
    void inject() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    @SaveSamples
    @Order(0)
    void whenSavedWithRootPathOnFixture() throws IOException {
        // The following call should be recorded:
        testService.getCat();

        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAMPLE_FILE);
    }

    @Test
    @Order(1)
    void thenFileShouldBeUnderTheRootPath() {
        assertThat(EXPECTED_SAMPLE_FILE).exists();
    }

    @Test
    @LoadSamples("/de/ppi/deepsampler/junit5/PersistentSamplerWithFixtureTest_whenSavedWithRootPathOnFixture.json")
    @Order(2)
    void andThenSavedFileWithRootOnFixtureMustBeLoadable() {
        final Cat sampledCat = testService.getCat();
        assertThat(sampledCat).isNotNull();
        assertThat(sampledCat.getName()).isEqualTo("Spot");
    }


    @SampleRootPath("./src/test/tmp")
    public static class TestSamplerFixture implements SamplerFixture {

        @PrepareSampler
        private TestService testServiceSampler;

        @Override
        public void defineSamplers() {
            PersistentSample.of(testServiceSampler.getCat()).hasId("cat");
        }
    }

}
