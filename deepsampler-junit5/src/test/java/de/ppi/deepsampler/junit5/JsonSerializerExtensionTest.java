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
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;

@ExtendWith(DeepSamplerExtension.class)
@UseSamplerFixture(JsonSerializerExtensionSamplerFixture.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JsonSerializerExtensionTest {

    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get("de", "ppi", "deepsampler", "junit5", "JsonSerializerExtensionTest_samplerCanBeSavedUsingAJsonExtension.json");
    public static final String SAVED_SAMPLER_FILE = "de/ppi/deepsampler/junit5/JsonSerializerExtensionTest_samplerCanBeSavedUsingAJsonExtension.json";

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
        testService.getDate();

        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_SAMPLER);
    }

    @Test
    @LoadSamples(file = SAVED_SAMPLER_FILE)
    @Order(1)
    void samplerCanBeLoadedUsingJsonExtension() {
        Date stubbedDate = testService.getDate();
        assertEquals("asd", stubbedDate.toString());
    }
}
