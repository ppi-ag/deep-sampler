/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;

import com.google.inject.Guice;
import de.ppi.deepsampler.core.api.PersistentSample;
import de.ppi.deepsampler.junit.FileSource;
import de.ppi.deepsampler.junit.GetSomeStringTestSampleFixture;
import de.ppi.deepsampler.junit.PrepareSampler;
import de.ppi.deepsampler.junit.SampleRootPath;
import de.ppi.deepsampler.junit.SamplerFixture;
import de.ppi.deepsampler.junit.TestModule;
import de.ppi.deepsampler.junit.TestSampleFixture;
import de.ppi.deepsampler.junit.TestService;
import de.ppi.deepsampler.junit.UseCharset;
import de.ppi.deepsampler.junit.UseSamplerFixture;
import de.ppi.deepsampler.junit.json.LoadSamples;
import de.ppi.deepsampler.junit.json.SaveSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedInt;
import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DeepSamplerExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@UseSamplerFixture(GetSomeStringTestSampleFixture.class)
@SampleRootPath("./src/test/tmp")
class PersistentSamplerTest {

    public static final Path DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH =
            Paths.get("./src/test/tmp/de/ppi/deepsampler/junit5/PersistentSamplerTest_whenSamplerWithDefaultPathIsSaved.json");

    public static final String SAVED_IN_SPECIFIC_FILE = "my/specific/package/samplerCanBeSavedInSpecificFile.json";
    public static final String SAVED_AS_CP1252 = "my/specific/package/cp1252.json";
    public static final Path SPECIFIC_PATH_WITH_SAMPLE_ROOT = Paths.get("./src/test/tmp").resolve(SAVED_IN_SPECIFIC_FILE);
    private static final Path FILE_CP1252 = Paths.get("./src/test/tmp").resolve(SAVED_AS_CP1252);

    public static final String LOAD_SPECIFIC_FILE_RELATIVE_TO_SAMPLE_ROOT = "../resources/de/ppi/deepsampler/junit5/samplerCanBeLoadedFromSpecificFile.json";
    public static final String LOAD_SPECIFIC_FILE_FROM_CLASS_PATH = "/de/ppi/deepsampler/junit5/samplerCanBeLoadedFromSpecificFile.json";

    @Inject
    private TestService testService;

    @BeforeEach
    public void inject() {
        Guice.createInjector(new TestModule()).injectMembers(this);
    }


    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(source = FileSource.CLASSPATH)
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
        assertThat(DEFAULT_PATH_INCLUDING_SAMPLE_ROOT_PATH).exists();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @SaveSamples(SAVED_IN_SPECIFIC_FILE)
    @Order(3)
    void whenSamplerIsSavedInSpecificFile() throws IOException {
        // Cleaning up a possibly existing file since we want to check that this file is
        // created by the annotation SaveFile after this test method has returned.
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(SPECIFIC_PATH_WITH_SAMPLE_ROOT);
    }

    @Test
    @Order(4)
    void thenSamplerMustBeFoundUnderRootPathWithSpecificPath() {
        assertTrue(Files.exists(SPECIFIC_PATH_WITH_SAMPLE_ROOT));
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(value = LOAD_SPECIFIC_FILE_RELATIVE_TO_SAMPLE_ROOT, source = FileSource.FILE_SYSTEM)
    @Order(7)
    void samplerCanBeLoadedFromSpecificFile() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @UseSamplerFixture(TestSampleFixture.class)
    @LoadSamples(value = LOAD_SPECIFIC_FILE_FROM_CLASS_PATH, source = FileSource.CLASSPATH)
    @Order(8)
    void samplerCanBeLoadedFromSpecificClasspathResource() throws Throwable {
        assertTestBeanHasStubbedInt();
    }

    @Test
    @LoadSamples(source = FileSource.CLASSPATH)
    @Order(9)
    void sampleFixtureFromClassLevelShouldBeUsed() throws Throwable {
        assertTestBeanHasStubbedString();
    }

    @Test
    @SaveSamples(SAVED_AS_CP1252)
    @UseSamplerFixture(TestSampleFixture.class)
    @UseCharset("cp1252")
    @Order(10)
    void whenSamplerIsSavedWithCharsetCp1252() throws IOException {
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(FILE_CP1252);

        // 🧪 WHEN
        recordSampleWithUmlaut();
    }

    @Test
    @LoadSamples(SAVED_AS_CP1252)
    @UseSamplerFixture(TestSampleFixture.class)
    @UseCharset("cp1252")
    @Order(11)
    void thenCp1252ShouldBeReadable() {
       assertThat(FILE_CP1252).content(Charset.forName("cp1252")).contains("ü");
       assertEquals("Spot ü", testService.getCat().getName());
    }

    @Test
    @LoadSamples(SAVED_AS_CP1252)
    @UseSamplerFixture(TestSampleFixture.class)
    @UseCharset("utf-8")
    @Order(12)
    void thenUtf8ShouldNotBeReadable() {
        assertNotEquals("Spot ü", testService.getCat().getName());
    }

    @Test
    @SaveSamples(SAVED_AS_CP1252)
    @UseSamplerFixture(SamplerFixtureWithCharsetOnClass.class)
    @Order(13)
    void whenSamplerCharsetConfigOnSamplerFixtureClass() throws IOException {
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(FILE_CP1252);
        // 🧪 WHEN
        recordSampleWithUmlaut();
    }

    private void recordSampleWithUmlaut() {
        // 👉 GIVEN
        testService.setCatsName("Spot ü");

        // 🧪 WHEN
        testService.getCat();
    }

    @Test
    @LoadSamples(SAVED_AS_CP1252)
    @UseSamplerFixture(SamplerFixtureWithCharsetOnMethod.class)
    @Order(14)
    void thenCharsetFromSamplerFixtureMethodShouldBeInFile() {
        assertThat(FILE_CP1252).content(Charset.forName("cp1252")).contains("ü");
        assertEquals("Spot ü", testService.getCat().getName());
    }

    @Test
    @SaveSamples(SAVED_AS_CP1252)
    @UseSamplerFixture(SamplerFixtureWithCharsetOnMethod.class)
    @Order(15)
    void whenSamplerCharsetConfigOnSamplerFixtureMethod() throws IOException {
        assertThatFileDoesNotExistOrOtherwiseDeleteIt(FILE_CP1252);

        // 🧪 WHEN
        recordSampleWithUmlaut();
    }

    @Test
    @LoadSamples(SAVED_AS_CP1252)
    @UseSamplerFixture(SamplerFixtureWithCharsetOnClass.class)
    @Order(16)
    void thenCharsetFromSamplerFixtureClassShouldBeInFile() {
        assertThat(FILE_CP1252).content(Charset.forName("cp1252")).contains("ü");
        assertEquals("Spot ü", testService.getCat().getName());
    }

    /**
     * Proves that path does not exist. However, if it exists, it is deleted.
     * @param path the path of the file that must not exist.
     * @throws IOException In case the file cannot be deleted.
     */
    public static void assertThatFileDoesNotExistOrOtherwiseDeleteIt(final Path path) throws IOException {
        if (Files.exists(path)) {
            Files.delete(path);
        }

        assertFalse(Files.exists(path));
    }

    @UseCharset("cp1252")
    public static class SamplerFixtureWithCharsetOnClass implements SamplerFixture {

        @PrepareSampler
        private TestService testServiceSampler;

        @Override
        public void defineSamplers() {
            PersistentSample.of(testServiceSampler.getCat());
        }
    }

    public static class SamplerFixtureWithCharsetOnMethod implements SamplerFixture {

        @PrepareSampler
        private TestService testServiceSampler;

        @UseCharset("cp1252")
        @Override
        public void defineSamplers() {
            PersistentSample.of(testServiceSampler.getCat());
        }
    }

}
