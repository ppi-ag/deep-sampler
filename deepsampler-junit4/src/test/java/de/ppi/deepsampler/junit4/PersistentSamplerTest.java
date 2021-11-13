/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit4;

import de.ppi.deepsampler.junit.LoadSamples;
import de.ppi.deepsampler.junit.SaveSamples;
import de.ppi.deepsampler.junit.TestSampleFixture;
import de.ppi.deepsampler.junit.UseSamplerFixture;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedInt;
import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistentSamplerTest {

    public static final Path EXPECTED_SAVED_SAMPLER = Paths.get("de", "ppi", "deepsampler", "junit4", "PersistentSamplerTest_bSamplerCanBeSaved.json");
    public static final String SAVED_IN_SPECIFIC_FILE_JSON = "de/ppi/deepsampler/junit4/samplerCanBeSavedInSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_JSON = "src/test/resources/de/ppi/deepsampler/junit4/fSamplerCanBeLoadedFromSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_FROM_CLASSPATH_JSON = "fSamplerCanBeLoadedFromSpecificFile.json";

    @Rule
    public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples
    public void aSamplerCanBeLoadedFromFile() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples
    public void bSamplerCanBeSaved() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_SAVED_SAMPLER);
    }

    @Test
    public void cTheSavedSamplerExists() {
        assertTrue(Files.exists(EXPECTED_SAVED_SAMPLER));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(file = SAVED_IN_SPECIFIC_FILE_JSON)
    public void dSamplerCanBeSavedInSpecificFile() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON));
    }

    @Test
    public void eSamplerHasBeenSavedInSpecificFileByPriorTestMethod() {
        assertTrue(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(file = SAVED_IN_SPECIFIC_FILE_JSON)
    public void fSamplerCanBeSavedInSpecificFileWithSpecificBuilder() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON));
    }

    @Test
    public void gSamplerHasBeenSavedInSpecificWithSpecificBuilderFileByPriorTestMethod() {
        assertTrue(Files.exists(Paths.get(SAVED_IN_SPECIFIC_FILE_JSON)));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(file = LOAD_SPECIFIC_FILE_JSON)
    public void fSamplerCanBeLoadedFromSpecificFile() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(classPath = LOAD_SPECIFIC_FILE_FROM_CLASSPATH_JSON)
    public void gSamplerCanBeLoadedFromSpecificClasspathResource() throws Throwable {
        assertTestBeanHasStubbedInt();
    }


}
