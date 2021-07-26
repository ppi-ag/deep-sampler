/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import de.ppi.deepsampler.core.api.*;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.persistence.api.PersistentSampleManager;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDateTime;

import static de.ppi.deepsampler.core.api.Matchers.*;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.combo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This TestClass must be be used to test all aop-provider in order to ensure that all providers would support the same
 * functionality.
 */
@SuppressWarnings("java:S5960")
public abstract class PersistentSamplerAspectTest {

    public static final String VALUE_A = "Value A";
    private static final TestBean TEST_BEAN_A = new TestBean();
    public static final String MY_ECHO_PARAMS = "MY ECHO PARAMS";
    public static final String NO_RETURN_VALUE_SAMPLE_ID = "NoReturnValue";


    /**
     * The {@link TestService} is a Service that is used to test method interception by a SamplerInterceptor. Since this class must be
     * instantiated by the concrete Dependency Injection Framework, the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance of {@link TestService} that has been created in a way that enables method interception by a particular AOP-framework (i.e. Spring).
     */
    public abstract TestService getTestService();


    @BeforeEach
    public void cleanUp() {
        Sampler.clear();
    }



    @Test
    public void samplesCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter(VALUE_A));
        PersistentSample.of(testServiceSampler.echoParameter(TEST_BEAN_A));

        getTestService().echoParameter(VALUE_A);
        getTestService().echoParameter(TEST_BEAN_A);
        final String pathToFile = "./record/samplesCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.echoParameter(VALUE_A));
        PersistentSample.of(testServiceSampler.echoParameter(TEST_BEAN_A));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertNotNull(getTestService().echoParameter(VALUE_A));
        assertNotNull(getTestService().echoParameter(TEST_BEAN_A));
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void voidMethodsCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        testServiceSampler.noReturnValue(2);

        getTestService().noReturnValue(2);
        getTestService().noReturnValue(3);
        final String pathToFile = "./record/voidMethodsCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));


        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        testServiceSampler.noReturnValue(2);
        source.load();
        getTestService().noReturnValue(2);

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sample.verifyCallQuantity(TestService.class, new FixedQuantity(1)).noReturnValue(2);
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void sqlDateCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        testServiceSampler.testSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));

        getTestService().testSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));
        final String pathToFile = "./record/sqlDateCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        testServiceSampler.testSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(new Date(1), getTestService().testSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C')));
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void listOfTestBeansReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getListOfTestBeans());

        getTestService().getListOfTestBeans();
        final String pathToFile = "./record/listOfTestBeansReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getListOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getListOfTestBeans().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getListOfTestBeans().get(0).getValue());

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void listOfStringsReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getListOfStrings());

        getTestService().getListOfStrings();
        final String pathToFile = "./record/listOfStringsReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getListOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getListOfStrings().size());

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void setOfStringsReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getSetOfStrings());

        getTestService().getSetOfStrings();
        final String pathToFile = "./record/setOfStringsReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getSetOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getSetOfStrings().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getSetOfStrings().toArray()[0]);

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void setOfTestBeansReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getSetOfTestBeans());

        getTestService().getSetOfTestBeans();
        final String pathToFile = "./record/setOfTestBeansReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getSetOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getSetOfTestBeans().size());
        TestBean testBean = getTestService().getSetOfTestBeans().stream().findFirst().get();
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, testBean.getValue());

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void arrayOfTestBeansReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfTestBeans());

        getTestService().getArrayOfTestBeans();
        final String pathToFile = "./record/arrayOfTestBeansReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getArrayOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getArrayOfTestBeans().length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getArrayOfTestBeans()[0].getValue());


        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void arrayOfStringsReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfStrings());

        getTestService().getArrayOfStrings();
        final String pathToFile = "./record/arrayOfStringsReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getArrayOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getArrayOfStrings().length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getArrayOfStrings()[0]);

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    void multidimensionalArrayOfStringsCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfStrings2d());

        getTestService().getArrayOfStrings2d();
        final String pathToFile = "./record/multidimensionalArrayCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getArrayOfStrings2d());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        String[][] result = getTestService().getArrayOfStrings2d();

        // THEN
        assertEquals(1, result.length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, result[0][0]);

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    void multidimensionalArrayOfTestBeansCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfTestBeans3d());

        getTestService().getArrayOfTestBeans3d();
        final String pathToFile = "./record/multidimensionalArrayOfTestBeansCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getArrayOfTestBeans3d());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        TestBean[][][] result = getTestService().getArrayOfTestBeans3d();

        // THEN
        assertEquals(1, result.length);
        assertEquals(2, result[0].length);
        assertEquals(1, result[0][0].length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, result[0][0][0].getValue());

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void mapOfStringsReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfStrings());

        getTestService().getMapOfStrings();
        final String pathToFile = "./record/mapOfStringsReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getMapOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfStrings().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getMapOfStrings().get(TestService.HARD_CODED_RETURN_VALUE));

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void mapOfStringsToTestBeansReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfStringsToTestBeans());

        getTestService().getMapOfStringsToTestBeans();
        final String pathToFile = "./record/mapOfStringsToTestBeansReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getMapOfStringsToTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfStringsToTestBeans().size());
        assertNotNull(getTestService().getMapOfStringsToTestBeans().get(TestService.HARD_CODED_RETURN_VALUE));

        TestBean loadedTestBean = getTestService().getMapOfStringsToTestBeans().get(TestService.HARD_CODED_RETURN_VALUE);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, loadedTestBean.getValue());

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void mapOfIntegersReturnValueCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfIntegers());

        getTestService().getMapOfIntegers();
        final String pathToFile = "./record/mapOfIntegersReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.getMapOfIntegers());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfIntegers().size());
        assertEquals(2, getTestService().getMapOfIntegers().get(1));

        Files.delete(Paths.get(pathToFile));
    }



    @Test
    public void manualIdSetForRecordingAndLoadingNoCorrectDef() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final String pathToFile = "./record/manualIdSetForRecordingAndLoading.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId("MY WRONG ECHO PARAMS");
        assertThrows(PersistenceException.class,
                source::load);

        assertTrue(SampleRepository.getInstance().isEmpty());
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDef() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final String pathToFile = "./record/manualIdSetForRecordingAndLoadingCorrectDef.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals("ABC", getTestService().echoParameter("ABC"));
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDefVoidMethod() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        testServiceSampler.noReturnValue(2);
        PersistentSample.setIdToLastMethodCall(NO_RETURN_VALUE_SAMPLE_ID);

        getTestService().noReturnValue(2);

        final String pathToFile = "./record/manualIdSetForRecordingAndLoadingCorrectDef.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        testServiceSampler.noReturnValue(2);
        PersistentSample.setIdToLastMethodCall(NO_RETURN_VALUE_SAMPLE_ID);
        source.load();
        getTestService().noReturnValue(2);

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(NO_RETURN_VALUE_SAMPLE_ID, SampleRepository.getInstance().getSamples().get(0).getSampleId());
        Sample.verifyCallQuantity(TestService.class, new FixedQuantity(1)).noReturnValue(2);
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void localDateTimeCanBeRecordedAndLoaded() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testLocalDateTime());

        getTestService().testLocalDateTime();

        final String pathToFile = "./record/localDateTimeCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        PersistentSample.of(testServiceSampler.testLocalDateTime());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(LocalDateTime.of(2020, 10, 29, 10, 10, 10), getTestService().testLocalDateTime());
        Files.delete(Paths.get(pathToFile));
    }




    @Test
    void testComboMatcherLoadAllButAcceptOnlyA() throws IOException {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter(anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final String pathToFile = "./record/comboMatcherSingleArgument.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();
        Sampler.clear();
        PersistentSample.of(testServiceSampler.echoParameter(combo(anyString(), (f, s) -> f.equals("A")))).hasId(MY_ECHO_PARAMS);

        source.load();

        // WHEN
        String result = getTestService().echoParameter("A");
        String secondCallResult = getTestService().echoParameter("A");
        String wrongParameter = getTestService().echoParameter("B");

        // THEN
        assertEquals("ABC", result);
        assertEquals("ABC", secondCallResult);
        assertEquals("B", wrongParameter);
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    void testComboMatcherSecondArgument() throws IOException {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.methodWithThreeParametersReturningLast(anyString(), anyString(), anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().methodWithThreeParametersReturningLast("BLOCK", "B", "R1");
        getTestService().methodWithThreeParametersReturningLast("NOBLOCK", "A", "R2");
        getTestService().methodWithThreeParametersReturningLast("BLOCK", "C", "R3");
        final String pathToFile = "./record/comboMatcherTwoArguments.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();
        Sampler.clear();
        PersistentSample.of(testServiceSampler.methodWithThreeParametersReturningLast(equalTo("BLOCK"), combo(anyString(), (f, s) -> f.equals("B")), combo(anyString(), (f, s) -> true))).hasId(MY_ECHO_PARAMS);

        source.load();

        // WHEN
        String resultFirst = getTestService().methodWithThreeParametersReturningLast("BLOCK", "C", "ABC1");
        String resultSecond = getTestService().methodWithThreeParametersReturningLast("BLOCK", "B", "ABC2");
        String resultThird = getTestService().methodWithThreeParametersReturningLast("NOBLOCK", "A", "ABC3");

        // THEN
        assertEquals("ABC1", resultFirst);
        assertEquals("R1", resultSecond);
        assertEquals("ABC3", resultThird);
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void mixPureJavaApiAndPersistenceApi() throws IOException {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testLocalDateTime());

        getTestService().testLocalDateTime();

        final String pathToFile = "./record/localDateTimeCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        // MIX persistence definition and pure java definition
        PersistentSample.of(testServiceSampler.testLocalDateTime());
        Sample.of(testServiceSampler.echoParameter("ABC")).is("CBD");
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(LocalDateTime.of(2020, 10, 29, 10, 10, 10), getTestService().testLocalDateTime());
        assertEquals("CBD", getTestService().echoParameter("ABC"));
        Files.delete(Paths.get(pathToFile));
    }

}