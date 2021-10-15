/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;


import com.google.inject.Guice;
import de.ppi.deepsampler.junit.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(DeepSamplerExtension.class)
@UseSamplerFixture(JsonSerializerExtensionSamplerFixture.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonSerializerExtensionTest {

    public static final String SAVED_SAMPLER_FILE = "de/ppi/deepsampler/junit5/JsonSerializerExtensionTest_samplerCanBeSavedUsingAJsonExtension.json";
    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get(SAVED_SAMPLER_FILE);

    @Inject
    private TestService testService;

    @BeforeEach
    void inject() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    @SaveSamples
    @Order(0)
    void samplerCanBeSavedUsingAJsonExtension() throws IOException {
        // Call the method that should be recorded
        testService.getInstant();

        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_SAMPLER);
    }

    @Test
    @LoadSamples(file = SAVED_SAMPLER_FILE)
    @Order(1)
    void samplerCanBeLoadedUsingJsonExtension() {
        final Instant stubbedInstant = testService.getInstant();
        assertNotNull(stubbedInstant);
    }
}
