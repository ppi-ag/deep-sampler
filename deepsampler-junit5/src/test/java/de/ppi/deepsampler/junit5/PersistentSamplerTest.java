/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;

import de.ppi.deepsampler.junit.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedInt;
import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedString;
import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DeepSamplerExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@UseSamplerFixture(GetSomeStringTestSampleFixture.class)
class PersistentSamplerTest {

    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get("de", "ppi", "deepsampler", "junit5", "PersistentSamplerTest_samplerCanBeSaved.json");
    public static final String SAVED_IN_SPECIFIC_FILE_JSON = "de/ppi/deepsampler/junit5/samplerCanBeSavedInSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_JSON = "src/test/resources/de/ppi/deepsampler/junit5/samplerCanBeLoadedFromSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_FROM_CLASSPATH_JSON = "samplerCanBeLoadedFromSpecificFile.json";


    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples
    @Order(0)
    void samplerCanBeLoadedFromFile() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples
    @Order(1)
    void samplerCanBeSaved() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_SAMPLER);
    }

    @Test
    @Order(2)
    void samplerHasBeenSavedByPriorTestMethod() {
        assertTrue(Files.exists(EXPECTED_SAVED_SAMPLER));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(file = SAVED_IN_SPECIFIC_FILE_JSON)
    @Order(3)
    void samplerCanBeSavedInSpecificFile() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON));
    }

    @Test
    @Order(4)
    void samplerHasBeenSavedInSpecificFileByPriorTestMethod() {
        assertTrue(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(file = SAVED_IN_SPECIFIC_FILE_JSON)
    @Order(5)
    void samplerCanBeSavedInSpecificFileWithSpecificBuilder() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON));
    }

    @Test
    @Order(6)
    void samplerHasBeenSavedInSpecificWithSpecificBuilderFileByPriorTestMethod() {
        assertTrue(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(file = LOAD_SPECIFIC_FILE_JSON)
    @Order(7)
    void samplerCanBeLoadedFromSpecificFile() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(classPath = LOAD_SPECIFIC_FILE_FROM_CLASSPATH_JSON)
    @Order(8)
    void samplerCanBeLoadedFromSpecificClasspathResource() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @LoadSamples
    @Order(9)
    void sampleFixtureFromClassLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasStubbedString();
    }


}
