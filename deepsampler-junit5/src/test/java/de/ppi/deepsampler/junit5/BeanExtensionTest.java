/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;


import com.google.inject.Guice;
import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.junit.*;
import de.ppi.deepsampler.persistence.bean.ext.OptionalExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(DeepSamplerExtension.class)
@UseSamplerFixture(BeanExtensionSamplerFixture.class)
@SampleRootPath("./src/test/tmp")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeanExtensionTest {

    public static final String SAVED_SAMPLER_FILE = "/de/ppi/deepsampler/junit5/samplerCanBeSavedUsingABeanExtension.json";
    public static final Path EXPECTED_SAVED_FILE_INCLUDING_ROOT_PATH = Paths.get("./src/test/tmp/").resolve(SAVED_SAMPLER_FILE);
    public static final String OPTIONAL_VALUE_SAMPLE_FILE = "/de/ppi/deepsampler/junit5/optionalValue.json";


    public static final String CATS_NAME_AS_IT_SHOULD_BE_RECORDED = "Cats name that should be recorded and written to the json file";
    public static final String CATS_NAME_FOR_CROSS_CHECK = "This name is unstubbed";


    @Inject
    private TestService testService;


    @BeforeEach
    public void inject() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    @SaveSamples(SAVED_SAMPLER_FILE)
    @Order(0)
    void samplerCanBeSavedUsingABeanExtension() throws IOException {
        // 👉 GIVEN
        testService.setCatsName(CATS_NAME_AS_IT_SHOULD_BE_RECORDED);

        // 🧪 WHEN
        // Call the method that should be recorded
        testService.getCat();

        // 🔬 THEN
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_FILE_INCLUDING_ROOT_PATH);
    }

    @Test
    @LoadSamples(SAVED_SAMPLER_FILE)
    @Order(1)
    void samplerCanBeLoadedUsingBeanExtension() {
        // 👉 GIVEN
        testService.setCatsName("This name should be overridden by the stub");

        // 🧪 WHEN
        final Cat stubbedCat = testService.getCat();

        //THEN
        assertNotNull(stubbedCat);
        assertEquals(CATS_NAME_AS_IT_SHOULD_BE_RECORDED, stubbedCat.getName());

        // CROSS-CHECK
        Sampler.clear();

        // 👉 GIVEN
        testService.setCatsName(CATS_NAME_FOR_CROSS_CHECK);

        // 🧪 WHEN
        final Cat unStubbedCat = testService.getCat();

        // 🔬 THEN
        assertNotNull(unStubbedCat);
        assertEquals(CATS_NAME_FOR_CROSS_CHECK, unStubbedCat.getName());
    }

    @Test
    @Order(2)
    @SaveSamples(OPTIONAL_VALUE_SAMPLE_FILE)
    void samplerWithOptionalValueCanBeRecorded() throws IOException {
        // 👉 GIVEN
        testService.setCatsName(CATS_NAME_AS_IT_SHOULD_BE_RECORDED);

        // 🧪 WHEN
        // Call the method that should be recorded
        testService.getOptionalCatsName();

        // 🔬 THEN
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_FILE_INCLUDING_ROOT_PATH);
    }

    @Test
    @Order(3)
    @LoadSamples(OPTIONAL_VALUE_SAMPLE_FILE)
    void samplerWithOptionalValueCanBeLoaded() {
        // 👉 GIVEN
        testService.setCatsName("This name should be overridden by the stub");

        // 🔬 THEN
        assertThat(testService.getOptionalCatsName())
                .isPresent()
                .hasValue(CATS_NAME_AS_IT_SHOULD_BE_RECORDED);
    }

    @Test
    @Order(4)
    @SaveSamples(OPTIONAL_VALUE_SAMPLE_FILE)
    void samplerWithOptionalEmptyCanBeRecorded() throws IOException {
        // 👉 GIVEN
        testService.setCatsName(null);

        // 🧪 WHEN
        // Call the method that should be recorded
        testService.getOptionalCatsName();

        // 🔬 THEN
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_FILE_INCLUDING_ROOT_PATH);
    }

    @Test
    @Order(5)
    @LoadSamples(OPTIONAL_VALUE_SAMPLE_FILE)
    void samplerWithOptionalEmptyCanBeLoaded() {
        // 👉 GIVEN
        testService.setCatsName("This name should be overridden by the stub");

        // 🔬 THEN
        assertThat(testService.getOptionalCatsName()).isEmpty();
    }

    @Test
    @Order(6)
    @SaveSamples(OPTIONAL_VALUE_SAMPLE_FILE)
    void samplerWithOptionalObjectCanBeRecorded() throws IOException {
        // 👉 GIVEN
        testService.setCatsName(CATS_NAME_AS_IT_SHOULD_BE_RECORDED);

        // 🧪 WHEN
        // Call the method that should be recorded
        testService.getOptionalCat();

        // 🔬 THEN
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_FILE_INCLUDING_ROOT_PATH);
    }

    @Test
    @Order(7)
    @LoadSamples(OPTIONAL_VALUE_SAMPLE_FILE)
    void samplerWithOptionalObjectCanBeLoaded() {
        // 👉 GIVEN
        testService.setCatsName("This name should be overridden by the stub");

        // 🔬 THEN
        assertThat(testService.getOptionalCat())
                .isPresent()
                .map(Cat::getName)
                .hasValue(CATS_NAME_AS_IT_SHOULD_BE_RECORDED);
    }

    @Test
    @Order(8)
    @SaveSamples(OPTIONAL_VALUE_SAMPLE_FILE)
    void samplerWithOptionalGenericObjectCanBeRecorded() throws IOException {
        // 👉 GIVEN
        testService.setCatsName(CATS_NAME_AS_IT_SHOULD_BE_RECORDED);

        // 🧪 WHEN
        // Call the method that should be recorded
        testService.getOptionalGenericCat();

        // 🔬 THEN
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_FILE_INCLUDING_ROOT_PATH);
    }

    @Test
    @Order(9)
    @LoadSamples(OPTIONAL_VALUE_SAMPLE_FILE)
    void samplerWithOptionalGenericObjectCanBeLoaded() {
        // 👉 GIVEN
        testService.setCatsName("This name should be overridden by the stub");

        // 🔬 THEN
        assertThat(testService.getOptionalGenericCat())
                .isPresent()
                .map(GenericCat::getPrey)
                .map(Dog::getName)
                .hasValue(CATS_NAME_AS_IT_SHOULD_BE_RECORDED);
    }
}