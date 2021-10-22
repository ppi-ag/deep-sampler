/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import de.ppi.deepsampler.core.api.FixedQuantity;
import de.ppi.deepsampler.core.api.PersistentSample;
import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.core.error.NoMatchingParametersFoundException;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.persistence.api.PersistentSampleManager;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDateTime;

import static de.ppi.deepsampler.core.api.FixedQuantity.ONCE;
import static de.ppi.deepsampler.core.api.Matchers.*;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.combo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This TestClass must be be used to test all aop-provider in order to ensure that all providers would support the same
 * functionality.
 */
@SuppressWarnings("java:S5960")
@ExtendWith(TempJsonFile.class)
public abstract class PersistentSamplerAspectTest {

    public static final String VALUE_A = "Value A";
    public static final String VALUE_B = "Value B";
    private static final TestBean TEST_BEAN_A = new TestBean();
    public static final String MY_ECHO_PARAMS = "MY ECHO PARAMS";
    public static final String NO_RETURN_VALUE_SAMPLE_ID = "NoReturnValue";
    public static final String BLOCK = "BLOCK";


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
    public void samplesCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter(VALUE_A));
        PersistentSample.of(testServiceSampler.echoParameter(TEST_BEAN_A));

        getTestService().echoParameter(VALUE_A);
        getTestService().echoParameter(TEST_BEAN_A);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter(VALUE_A));
        PersistentSample.of(testServiceSampler.echoParameter(TEST_BEAN_A));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertNotNull(getTestService().echoParameter(VALUE_A));
        assertNotNull(getTestService().echoParameter(TEST_BEAN_A));
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));
    }



    @Test
    public void voidMethodsCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.forVerification(testServiceSampler).noReturnValue(anyInt());

        getTestService().noReturnValue(2);
        getTestService().noReturnValue(3);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.forVerification(testServiceSampler).noReturnValue(anyInt());
        source.load();

        getTestService().noReturnValue(2);
        getTestService().noReturnValue(3);

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sample.verifyCallQuantity(TestService.class, ONCE).noReturnValue(2);
    }

    @Test
    public void sqlDateCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C')));

        Date expectedDate = getTestService().testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C')));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());

        Date stubbedDate = getTestService().testRandomSqlDate(new RecTestBean(new RecTestBean(null, "A", 'C'), "B", 'C'));
        assertEquals(expectedDate, stubbedDate);
    }

    @Test
    public void listOfTestBeansReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getListOfTestBeans());

        getTestService().getListOfTestBeans();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getListOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getListOfTestBeans().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getListOfTestBeans().get(0).getValue());
    }

    @Test
    public void listOfStringsReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getListOfStrings());

        getTestService().getListOfStrings();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getListOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getListOfStrings().size());
    }

    @Test
    public void setOfStringsReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getSetOfStrings());

        getTestService().getSetOfStrings();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getSetOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getSetOfStrings().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getSetOfStrings().toArray()[0]);
    }

    @Test
    public void setOfTestBeansReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getSetOfTestBeans());

        // WHEN
        getTestService().getSetOfTestBeans();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getSetOfTestBeans());

        source.load();

        // THEN
        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getSetOfTestBeans().size());

        assertThat(getTestService().getSetOfTestBeans().stream().findFirst())
                .isPresent()
                .map(TestBean::getValue).hasValue(TestService.HARD_CODED_RETURN_VALUE);
    }

    @Test
    public void arrayOfTestBeansReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfTestBeans());

        getTestService().getArrayOfTestBeans();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getArrayOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getArrayOfTestBeans().length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getArrayOfTestBeans()[0].getValue());
    }

    @Test
    public void arrayOfStringsReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfStrings());

        getTestService().getArrayOfStrings();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getArrayOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getArrayOfStrings().length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getArrayOfStrings()[0]);
    }

    @Test
    void multidimensionalArrayOfStringsCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfStrings2d());

        getTestService().getArrayOfStrings2d();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getArrayOfStrings2d());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());

        // WHEN
        String[][] result = getTestService().getArrayOfStrings2d();

        // THEN
        assertEquals(1, result.length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, result[0][0]);
    }



    @Test
    void multidimensionalArrayOfTestBeansCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getArrayOfTestBeans3d());

        getTestService().getArrayOfTestBeans3d();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

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
    }

    @Test
    public void mapOfStringsReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfStrings());

        getTestService().getMapOfStrings();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getMapOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfStrings().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getMapOfStrings().get(TestService.HARD_CODED_RETURN_VALUE));
    }

    @Test
    public void mapOfStringsToTestBeansReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfStringsToTestBeans());

        getTestService().getMapOfStringsToTestBeans();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getMapOfStringsToTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfStringsToTestBeans().size());
        assertNotNull(getTestService().getMapOfStringsToTestBeans().get(TestService.HARD_CODED_RETURN_VALUE));

        TestBean loadedTestBean = getTestService().getMapOfStringsToTestBeans().get(TestService.HARD_CODED_RETURN_VALUE);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, loadedTestBean.getValue());
    }

    @Test
    public void mapOfIntegersReturnValueCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getMapOfIntegers());

        getTestService().getMapOfIntegers();
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getMapOfIntegers());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfIntegers().size());
        assertEquals(2, getTestService().getMapOfIntegers().get(1));
    }


    @Test
    public void callsWithNotMatchingParametersAreRoutedToOriginalMethod(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getRandom(VALUE_A));

        String hopefullyRecordedValue = getTestService().getRandom(VALUE_A);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getRandom(VALUE_A));
        Sample.of(testServiceSampler.getRandom(anyString())).callsOriginalMethod();
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(hopefullyRecordedValue, getTestService().getRandom(VALUE_A));
        assertNotEquals(hopefullyRecordedValue, getTestService().getRandom(VALUE_B));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingNoCorrectDef(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId("MY WRONG ECHO PARAMS");
        assertThrows(PersistenceException.class,
                source::load);
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDef(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals("ABC", getTestService().echoParameter("ABC"));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDefVoidMethod(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.forVerification(testServiceSampler).noReturnValue(2);
        PersistentSample.setIdToLastMethodCall(NO_RETURN_VALUE_SAMPLE_ID);

        getTestService().noReturnValue(2);

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.forVerification(testServiceSampler).noReturnValue(2);
        PersistentSample.setIdToLastMethodCall(NO_RETURN_VALUE_SAMPLE_ID);
        source.load();
        getTestService().noReturnValue(2);

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(NO_RETURN_VALUE_SAMPLE_ID, SampleRepository.getInstance().getSamples().get(0).getSampleId());
        Sample.verifyCallQuantity(TestService.class, new FixedQuantity(1)).noReturnValue(2);
    }

    @Test
    public void localDateTimeCanBeRecordedAndLoaded(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testLocalDateTime());

        getTestService().testLocalDateTime();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.testLocalDateTime());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(LocalDateTime.of(2020, 10, 29, 10, 10, 10), getTestService().testLocalDateTime());
    }




    @Test
    void testComboMatcherLoadAllButAcceptOnlyA(Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.echoParameter(anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.echoParameter(combo(anyString(), (f, s) -> f.equals("A")))).hasId(MY_ECHO_PARAMS);

        source.load();

        // WHEN
        final TestService testService = getTestService();
        String result = testService.echoParameter("A");
        String secondCallResult = testService.echoParameter("A");

        // THEN
        assertEquals("ABC", result);
        assertEquals("ABC", secondCallResult);
        assertThrows(NoMatchingParametersFoundException.class, () -> testService.echoParameter("B"));
    }

    @Test
    void testComboMatcherSecondArgument(Path tempFile) {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.methodWithThreeParametersReturningLast(anyString(), anyString(), anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().methodWithThreeParametersReturningLast(BLOCK, "B", "R1");
        getTestService().methodWithThreeParametersReturningLast(BLOCK, "C", "R3");

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.methodWithThreeParametersReturningLast(equalTo(BLOCK), combo(anyString(), (f, s) -> f.equals("B")), combo(anyString(), (f, s) -> true))).hasId(MY_ECHO_PARAMS);
        source.load();

        // WHEN
        final TestService testService = getTestService();
        String result = testService.methodWithThreeParametersReturningLast(BLOCK, "B", "ABC2");

        // THEN
        assertThrows(NoMatchingParametersFoundException.class, () -> testService.methodWithThreeParametersReturningLast(BLOCK, "C", "ABC1"));
        assertEquals("R1", result);
    }

    @Test
    public void byteArrayCanBeRecordedAndLoaded(Path tempFile) {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.getRandomByteArray(anyInt()));

        byte[] expectedArray = getTestService().getRandomByteArray(42);

        final PersistentSampleManager source = save(tempFile);
        clearSampleRepositoryWithAssertion();

        PersistentSample.of(testServiceSampler.getRandomByteArray(anyInt()));

        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        byte[] valueStubbedMethod =  getTestService().getRandomByteArray(42);
        assertArrayEquals(expectedArray ,valueStubbedMethod);
    }

    @Test
    public void mixPureJavaApiAndPersistenceApi(Path tempFile) {
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        PersistentSample.of(testServiceSampler.testLocalDateTime());

        getTestService().testLocalDateTime();

        final PersistentSampleManager source = save(tempFile);

        clearSampleRepositoryWithAssertion();

        // MIX persistence definition and pure java definition
        PersistentSample.of(testServiceSampler.testLocalDateTime());
        Sample.of(testServiceSampler.echoParameter("ABC")).is("CBD");
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(LocalDateTime.of(2020, 10, 29, 10, 10, 10), getTestService().testLocalDateTime());
        assertEquals("CBD", getTestService().echoParameter("ABC"));
    }

    private void clearSampleRepositoryWithAssertion() {
        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());
    }

    private PersistentSampleManager save(Path pathToFile) {
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();
        return source;
    }

}