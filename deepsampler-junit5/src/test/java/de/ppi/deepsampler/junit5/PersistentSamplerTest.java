/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
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
@SampleRootPath("./src/test/tmp")
class PersistentSamplerTest {

    public static final Path DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH =
            Paths.get("./src/test/tmp/de/ppi/deepsampler/junit5/PersistentSamplerTest_whenSamplerWithDefaultPathIsSaved.json");

    public static final String SAVED_IN_SPECIFIC_PACKAGE = "my/specific/package";
    public static final String SAVED_IN_SPECIFIC_FILE = "samplerCanBeSavedInSpecificFile.json";
    public static final Path SPECIFIC_PATH = Paths.get("./src/test/tmp").resolve(SAVED_IN_SPECIFIC_PACKAGE).resolve(SAVED_IN_SPECIFIC_FILE);

    public static final String LOAD_SPECIFIC_PACKAGE_FOR_FILE_SYSTEM = "../resources/de/ppi/deepsampler/junit5";
    public static final String LOAD_SPECIFIC_FILE = "samplerCanBeLoadedFromSpecificFile.json";


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
    void whenSamplerWithDefaultPathIsSaved() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH);
    }

    @Test
    @Order(2)
    void thenSamplerMustBeFoundUnderRootPathWithDefaultPackageAndFileName() {
        assertTrue(Files.exists(DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(packagePath = SAVED_IN_SPECIFIC_PACKAGE, fileName = SAVED_IN_SPECIFIC_FILE)
    @Order(3)
    void whenSamplerIsSavedInSpecificPackageAndFile() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(SPECIFIC_PATH);
    }

    @Test
    @Order(4)
    void thenSamplerMustBeFoundUnderRootPathWithSpecificPath() {
        assertTrue(Files.exists(SPECIFIC_PATH));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(packagePath = LOAD_SPECIFIC_PACKAGE_FOR_FILE_SYSTEM, fileName = LOAD_SPECIFIC_FILE, source = FileSource.FILE_SYSTEM)
    @Order(7)
    void samplerCanBeLoadedFromSpecificFile() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(fileName = LOAD_SPECIFIC_FILE, source = FileSource.CLASSPATH)
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
