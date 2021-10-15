/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit4;


import com.google.inject.Guice;
import de.ppi.deepsampler.junit.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.junit.Assert.assertNotNull;

@UseSamplerFixture(JsonSerializerExtensionSamplerFixture.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonSerializerExtensionTest {

    public static final String SAVED_SAMPLER_FILE = "de/ppi/deepsampler/junit4/JsonSerializerExtensionTest_aSamplerCanBeSavedUsingAJsonExtension.json";
    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get(SAVED_SAMPLER_FILE);

    @Inject
    private TestService testService;

    @Rule
    public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();

    @Before
    public void inject() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    @SaveSamples
    public void aSamplerCanBeSavedUsingAJsonExtension() throws IOException {
        // Call the method that should be recorded
        testService.getInstant();

        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_SAMPLER);
    }

    @Test
    @LoadSamples(file = SAVED_SAMPLER_FILE)
    public void bSamplerCanBeLoadedUsingJsonExtension() {
        final Instant stubbedInstant = testService.getInstant();
        assertNotNull(stubbedInstant);
    }
}
