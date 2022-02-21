/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;


import com.google.inject.Guice;
import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.junit.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(DeepSamplerExtension.class)
@UseSamplerFixture(JsonSerializerExtensionSamplerFixture.class)
@SampleRootPath("./src/test/tmp")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonSerializerExtensionTest {

    public static final String SAVED_SAMPLER_FILE = "/de/ppi/deepsampler/junit5/samplerCanBeSavedUsingAJsonExtension.json";
    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get("./src/test/tmp/").resolve(SAVED_SAMPLER_FILE);

    private static final Instant STUBBED_INSTANT = LocalDateTime.of(2021, 10, 15, 17, 45).toInstant(ZoneOffset.UTC);
    private static final Instant UN_STUBBED_INSTANT = LocalDateTime.of(2000, 1, 1, 12, 0).toInstant(ZoneOffset.UTC);

    @Inject
    private TestService testService;

    @BeforeEach
    void inject() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    @SaveSamples(SAVED_SAMPLER_FILE)
    @Order(0)
    void samplerCanBeSavedUsingAJsonExtension() throws IOException {
        // GIVEN
        testService.setDefaultInstant(STUBBED_INSTANT);

        // WHEN
        // Call the method that should be recorded
        testService.getInstant();

        // THEN
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_SAMPLER);
    }

    @Test
    @LoadSamples(SAVED_SAMPLER_FILE)
    @Order(1)
    void samplerCanBeLoadedUsingJsonExtension() {
        // GIVEN
        testService.setDefaultInstant(UN_STUBBED_INSTANT);

        // WHEN
        final Instant stubbedInstant = testService.getInstant();

        // THEN
        assertNotNull(stubbedInstant);
        assertEquals(STUBBED_INSTANT, stubbedInstant);

        // CROSS-CHECK
        Sampler.clear();

        // GIVEN
        testService.setDefaultInstant(UN_STUBBED_INSTANT);

        // WHEN
        final Instant unStubbedInstant = testService.getInstant();

        // THEN
        assertNotNull(unStubbedInstant);
        assertEquals(UN_STUBBED_INSTANT, unStubbedInstant);
    }
}
