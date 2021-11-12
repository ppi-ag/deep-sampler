/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
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

    public static final String SAVED_IN_SPECIFIC_PACKAGE = "my/specific/package";
    public static final String SAVED_IN_SPECIFIC_FILE = "samplerCanBeSavedInSpecificFile.json";
    public static final Path SPECIFIC_PATH = Paths.get("./src/test/tmp").resolve(SAVED_IN_SPECIFIC_PACKAGE).resolve(SAVED_IN_SPECIFIC_FILE);

    public static final String LOAD_SPECIFIC_PACKAGE_FOR_FILE_SYSTEM = "../resources/de/ppi/deepsampler/junit4";
    public static final String LOAD_SPECIFIC_FILE = "fSamplerCanBeLoadedFromSpecificFile.json";

    public static final String FILENAME_WITH_FOLDER = "my/folder/mySampler.json";
    public static final Path EXPECTED_FILENAME_WITH_FOLDER = Paths.get("./src/test/tmp").resolve(FILENAME_WITH_FOLDER);

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
    public void b1WhenSamplerWithDefaultPathIsSaved() throws IOException {
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH);
    }

    @Test
    public void b2ThenAFileUnderRootPathAndInDefaultFoldersMustExist() {
        assertTrue("Die Datei: " + DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH + " wurde nicht gefunden.", Files.exists(DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(packagePath = SAVED_IN_SPECIFIC_PACKAGE, fileName = SAVED_IN_SPECIFIC_FILE)
    public void c1WhenSamplerIsSavedInSpecificPackageAndFile() throws IOException {
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(SPECIFIC_PATH);
    }

    @Test
    public void c2ThenSamplerMustBeFoundUnderRootPathAndSpecificFolder() {
        assertTrue("Die Datei " + SPECIFIC_PATH + " konnte nicht gefunden werden.", Files.exists(SPECIFIC_PATH));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(packagePath = LOAD_SPECIFIC_PACKAGE_FOR_FILE_SYSTEM, fileName = LOAD_SPECIFIC_FILE, source = FileSource.FILE_SYSTEM)
    public void dSamplerCanBeLoadedFromSpecificFileAndOverrideSampleRootPath() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(fileName = LOAD_SPECIFIC_FILE, source = FileSource.CLASSPATH)
    public void eSamplerCanBeLoadedFromSpecificClasspathResource() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @LoadSamples
    public void fSampleFixtureFromClassLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasStubbedString();
    }

    /**
     * Is it possible to save a file using a fileName that also includes a path, not only a pure filename?
     * Even if packagePath is set to empty?
     * @throws IOException
     */
    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(packagePath = "", fileName = FILENAME_WITH_FOLDER)
    public void g1WhenSamplerIsSavedWithFileNameIncludingAFolder() throws IOException {
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(EXPECTED_FILENAME_WITH_FOLDER);
    }

    /**
     * We expect, that the preceding test has written a file under the SampleRootPath using only the fileName.
     * And the fileName should include folders.
     */
    @Test
    public void g2ThenSamplerMustBeFoundUnderRootPathWithFolderFromFileName() {
        assertTrue("Die Datei " + EXPECTED_FILENAME_WITH_FOLDER + " konnte nicht gefunden werden.", Files.exists(EXPECTED_FILENAME_WITH_FOLDER));
    }
}
