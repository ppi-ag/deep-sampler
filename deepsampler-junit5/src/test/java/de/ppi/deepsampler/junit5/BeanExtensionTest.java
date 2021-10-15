/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
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

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DeepSamplerExtension.class)
@UseSamplerFixture(BeanExtensionSamplerFixture.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeanExtensionTest {

    public static final String SAVED_SAMPLER_FILE = "de/ppi/deepsampler/junit5/BeanExtensionTest_samplerCanBeSavedUsingABeanExtension.json";
    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get(SAVED_SAMPLER_FILE);

    public static final String CATS_NAME_AS_IT_SHOULD_BE_RECORDED = "Cats name that should be recorded and written to the json file";
    public static final String CATS_NAME_FOR_CROSS_CHECK = "This name is unstubbd";


    @Inject
    private TestService testService;


    @BeforeEach
    public void inject() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    @SaveSamples
    @Order(0)
    public void samplerCanBeSavedUsingABeanExtension() throws IOException {
        // GIVEN
        testService.setCatsName(CATS_NAME_AS_IT_SHOULD_BE_RECORDED);

        // WHEN
        // Call the method that should be recorded
        testService.getCat();

        // THEN
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_SAMPLER);
    }

    @Test
    @LoadSamples(file = SAVED_SAMPLER_FILE)
    @Order(1)
    public void samplerCanBeLoadedUsingBeanExtension() {
        // GIVEN
        testService.setCatsName("This name should be overridden by the stub");

        // WHEN
        final Cat stubbedCat = testService.getCat();

        //THEN
        assertNotNull(stubbedCat);
        assertEquals(CATS_NAME_AS_IT_SHOULD_BE_RECORDED, stubbedCat.getName());

        // CROSS-CHECK
        Sampler.clear();

        // GIVEN
        testService.setCatsName(CATS_NAME_FOR_CROSS_CHECK);

        // WHEN
        final Cat unStubbedCat = testService.getCat();

        // THEN
        assertNotNull(unStubbedCat);
        assertEquals(CATS_NAME_FOR_CROSS_CHECK, unStubbedCat.getName());

    }
}