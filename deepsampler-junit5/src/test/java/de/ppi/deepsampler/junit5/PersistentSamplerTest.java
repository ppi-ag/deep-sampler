/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;

import com.google.inject.Guice;
import com.sun.org.apache.xerces.internal.impl.xs.util.StringListImpl;
import de.ppi.deepsampler.junit.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedInt;
import static de.ppi.deepsampler.junit.JUnitTestUtility.assertTestBeanHasStubbedString;
import static de.ppi.deepsampler.junit.JUnitTestUtility.assertThatFileDoesNotExistOrOtherwiseDeleteIt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.*;

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
    void whenSamplerIsSavedWithCharsetCp1252() {
        // 👉 GIVEN
        testService.setCatsName("Spot ü");

        // 🧪 WHEN
        testService.getCat();
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
    @Order(11)
    void thenUtf8ShouldNotBeReadable() {
        assertNotEquals("Spot ü", testService.getCat().getName());
    }

}
