/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit4;

import de.ppi.deepsampler.junit.*;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.ppi.deepsampler.junit.JUnitTestUtility.*;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@UseSamplerFixture(GetSomeStringTestSampleFixture.class)
@SampleRootPath("./src/test/tmp")
public class PersistentSamplerTest {

    public static final Path DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH =
            Paths.get("./src/test/tmp/de/ppi/deepsampler/junit4/PersistentSamplerTest_b1WhenSamplerWithDefaultPathIsSaved.json");

    public static final String SAVED_IN_SPECIFIC_FILE = "my/specific/package/samplerCanBeSavedInSpecificFile.json";
    public static final Path SPECIFIC_PATH = Paths.get("./src/test/tmp").resolve(SAVED_IN_SPECIFIC_FILE);

    public static final String LOAD_SPECIFIC_FILE_RELATIVE_TO_SAMPLE_ROOT_PATH = "../resources/de/ppi/deepsampler/junit4/fSamplerCanBeLoadedFromSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_FROM_CLASS_PATH = "/de/ppi/deepsampler/junit4/fSamplerCanBeLoadedFromSpecificFile.json";

    @Rule
    public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(source = FileSource.CLASSPATH)
    public void aSamplerCanBeLoadedFromFile() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples
    public void b1WhenSamplerWithDefaultPathIsSaved() throws IOException {
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH);
    }

    @Test
    public void b2ThenAFileUnderRootPathAndInDefaultFoldersMustExist() {
        assertTrue("The file: " + DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH + " is missing.", Files.exists(DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(SAVED_IN_SPECIFIC_FILE)
    public void c1WhenSamplerIsSavedInSpecificFile() throws IOException {
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(SPECIFIC_PATH);
    }

    @Test
    public void c2ThenSamplerMustBeFoundUnderRootPathAndSpecificFile() {
        assertTrue("The file " + SPECIFIC_PATH + " is missing.", Files.exists(SPECIFIC_PATH));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(value = LOAD_SPECIFIC_FILE_RELATIVE_TO_SAMPLE_ROOT_PATH, source = FileSource.FILE_SYSTEM)
    public void dSamplerCanBeLoadedFromSpecificFileAndOverrideSampleRootPath() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(value = LOAD_SPECIFIC_FILE_FROM_CLASS_PATH, source = FileSource.CLASSPATH)
    public void eSamplerCanBeLoadedFromSpecificClasspathResource() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @LoadSamples(source = FileSource.CLASSPATH)
    public void fSampleFixtureFromClassLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasStubbedString();
    }
}
