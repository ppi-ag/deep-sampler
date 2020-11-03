package org.deepsampler.provider.common;

import org.deepsampler.core.api.Sample;
import org.deepsampler.core.api.Sampler;
import org.deepsampler.core.error.VerifyException;
import org.deepsampler.core.internal.FixedQuantity;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.persistence.JsonSourceManager;
import org.deepsampler.persistence.api.PersistentSampleManager;
import org.deepsampler.persistence.api.PersistentSampler;
import org.deepsampler.persistence.error.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;

import static org.deepsampler.core.api.Matchers.anyString;
import static org.deepsampler.core.api.Matchers.equalTo;
import static org.deepsampler.core.internal.FixedQuantity.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This Testclass must be be used to test all aop-provider in order to ensure that all providers would support the same
 * functionality.
 */
public abstract class SamplerInterceptorTest {

    public static final String VALUE_A = "Value A";
    public static final String VALUE_B = "Value B";
    public static final String VALUE_C = "Value C";
    public static final int INT_VALUE = 42;
    private static final TestBean TEST_BEAN_A = new TestBean();
    private static final TestBean TEST_BEAN_B = new TestBean();
    public static final String MYECHOPARAMS = "MYECHOPARAMS";

    /**
     * The {@link TestService} is a Service that is used to test method interception by a SamplerInterceptor. Since this class must be
     * instantiated by the concrete Dependency Injection Framework, the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance of {@link TestService} that has been created in a way that enables method interception by a particular AOP-framework (i.e. Spring).
     */
    public abstract TestService getTestService();

    /**
     * The {@link TestServiceContainer} delegates to {@link TestService} and is used to test deeper object trees.
     * Since this class must be instantiated by the concrete Dependency Injection Framework, the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance of {@link TestService} that has been created in a way that enables method interception by a particular AOP-framework (i.e. Spring).
     */
    public abstract TestServiceContainer getTestServiceContainer();

    @BeforeEach
    public void cleanUp() {
        Sampler.clear();
    }

    @Test
    public void singleArgumentValueMatchesAndSampleIsChanged() {
        //WHEN UNCHANGED
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        //THEN
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_B));
    }


    @Test
    public void multipleSamplerAreHandledDistinct() {
        //WHEN UNCHANGED
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        // This sampler is not used, it is here to check if the sequence of preparing has any impact on Sample.of(). That should not happen.
        Sampler.prepare((TestBean.class));

        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        //THEN
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_B));
    }

    @Test
    public void singleArgumentValueDoesNotMatchAndSampleIsNotChanged() {
        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        //THEN
        assertEquals(VALUE_C, getTestService().echoParameter(VALUE_C));
    }

    @Test
    public void methodHasNoSampleAndIsNotChanged() {
        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        //THEN
        assertEquals(TEST_BEAN_A, getTestService().echoParameter(TEST_BEAN_A));
    }

    @Test
    public void methodWithNoParameterShouldChangeItsBehavior() {
        //WHEN UNCHANGED
        assertEquals(-1, getTestService().getMinusOne());

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getMinusOne()).is(INT_VALUE);

        //THEN
        assertEquals(INT_VALUE, getTestService().getMinusOne());
    }

    @Test
    public void singleBeanArgumentValueMatchesAndSampleIsChanged() {
        // WHEN UNCHANGED
        assertEquals(TEST_BEAN_A, getTestService().echoParameter(TEST_BEAN_A));

        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A)).is(TEST_BEAN_B);

        //THEN
        assertEquals(TEST_BEAN_B, getTestService().echoParameter(TEST_BEAN_A));
    }

    @Test
    public void deepObjectSampleIsChanged() {
        final TestServiceContainer testServiceContainer = getTestServiceContainer();

        // WHEN UNCHANGED
        assertEquals(VALUE_C + TestServiceContainer.SUFFIX_FROM_SERVICE_CONTAINER, testServiceContainer.augmentValueFromTestService());

        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_C)).is(VALUE_B);

        //THEN
        assertEquals(VALUE_B + TestServiceContainer.SUFFIX_FROM_SERVICE_CONTAINER, testServiceContainer.augmentValueFromTestService());
    }

    @Test
    public void samplesCanUseTheAnyMatcher() {
        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(anyString())).is(VALUE_A);

        //THEN
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_B));
    }

    @Test
    public void methodWithTwoParameterCanBeSampled() {
        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.methodWithTwoParameter("a", "b")).is(VALUE_A);

        assertEquals(VALUE_A, getTestService().methodWithTwoParameter("a", "b"));
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().methodWithTwoParameter("x", "b"));
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().methodWithTwoParameter("a", "x"));

    }

    @Test
    public void samplesCanUseAMixedCombinationOfMatchers() {
        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.methodWithTwoParameter(anyString(), equalTo("Expected parameter value"))).is(VALUE_A);

        //THEN
        assertEquals(VALUE_A, getTestService().methodWithTwoParameter("Some uninspired random value", "Expected parameter value"));
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().methodWithTwoParameter("Some uninspired random value", "wrong"));
    }

    @Test
    public void verifyMethodNotCalled() {
        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A)).is(TEST_BEAN_B);

        // CALL
        getTestService().echoParameter(TEST_BEAN_B);

        //THEN
        Sample.verifyCallQuantity(TestService.class, NEVER).echoParameter(TEST_BEAN_A);
        Sample.verifyCallQuantity(TestService.class, NEVER).getMinusOne();
    }

    @Test
    public void verifyMethodCalledOnce() {
        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A)).is(TEST_BEAN_B);

        // CALL
        getTestService().echoParameter(TEST_BEAN_A);

        //THEN
        Sample.verifyCallQuantity(TestService.class, ONCE).echoParameter(TEST_BEAN_A);
        Sample.verifyCallQuantity(TestService.class, NEVER).echoParameter(TEST_BEAN_B);
        Sample.verifyCallQuantity(TestService.class, NEVER).getMinusOne();
    }

    @Test
    public void verifyMethodCalledMultipleAndMixed() {
        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_B)).is(TEST_BEAN_B);
        Sample.of(testServiceSampler.getMinusOne()).is(1);

        // CALL
        getTestService().echoParameter(TEST_BEAN_B);
        getTestService().echoParameter(TEST_BEAN_B);
        getTestService().getMinusOne();

        //THEN
        Sample.verifyCallQuantity(TestService.class, NEVER).echoParameter(TEST_BEAN_A);
        Sample.verifyCallQuantity(TestService.class, TWICE).echoParameter(TEST_BEAN_B);
        Sample.verifyCallQuantity(TestService.class, ONCE).getMinusOne();
    }

    @Test
    public void verifyMethodWrongNumber() {
        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A)).is(TEST_BEAN_B);

        // CALL
        getTestService().getMinusOne();
        getTestService().getMinusOne();

        //THEN
        assertThrows(VerifyException.class, () -> Sample.verifyCallQuantity(TestService.class, new FixedQuantity(1))
                .getMinusOne());
    }

    @Test
    public void verifyMethodCalledWithoutSample() {
        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_B));
        Sample.of(testServiceSampler.getMinusOne());

        // CALL
        getTestService().echoParameter(TEST_BEAN_B);
        getTestService().echoParameter(TEST_BEAN_B);
        getTestService().getMinusOne();

        //THEN
        Sample.verifyCallQuantity(TestService.class, NEVER).echoParameter(TEST_BEAN_A);
        Sample.verifyCallQuantity(TestService.class, TWICE).echoParameter(TEST_BEAN_B);
        Sample.verifyCallQuantity(TestService.class, ONCE).getMinusOne();
    }

    @Test
    public void verifyVoidMethod() {
        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.forVerification(testServiceSampler).noReturnValue(1);

        //CALL
        getTestService().noReturnValue(1);

        //THEN
        Sample.verifyCallQuantity(TestService.class, ONCE).noReturnValue(1);
    }


    @Test
    public void verifyVoidMethodWithWrongParameter() {
        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.forVerification(testServiceSampler).noReturnValue(1);

        //CALL
        getTestService().noReturnValue(1);

        //THEN
        assertThrows(VerifyException.class, () -> Sample.verifyCallQuantity(TestService.class, ONCE).noReturnValue(2), "The sampled method public void org.deepsampler.provider.common.TestService.noReturnValue(int) that was expected to be called with (2) was actually called with (1) (1 times).");
    }

    @Test
    public void samplesCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_A));

        getTestService().echoParameter(VALUE_A);
        String pathToFile = "./record/samplesCanBeRecordedAndLoaded.json";
        PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder(pathToFile).build());
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.echoParameter(VALUE_A));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertNotNull(getTestService().echoParameter(VALUE_A));
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void voidMethodsCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        testServiceSampler.noReturnValue(2);

        getTestService().noReturnValue(2);
        getTestService().noReturnValue(3);
        String pathToFile = "./record/voidMethodsCanBeRecordedAndLoaded.json";
        PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder(pathToFile).build());
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
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        testServiceSampler.testSqlDate(new RecTestBean(new RecTestBean(null, "A"), "B"));

        getTestService().testSqlDate(new RecTestBean(new RecTestBean(null, "A"), "B"));
        String pathToFile = "./record/sqlDateCanBeRecordedAndLoaded.json";
        PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder(pathToFile).build());
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        testServiceSampler.testSqlDate(new RecTestBean(new RecTestBean(null, "A"), "B"));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(new Date(1), getTestService().testSqlDate(new RecTestBean(new RecTestBean(null, "A"), "B")));
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingNoCorrectDef() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter("ABC")).hasId(MYECHOPARAMS);

        getTestService().echoParameter("ABC");
        String pathToFile = "./record/manualIdSetForRecordingAndLoading.json";
        PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder(pathToFile).build());
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.echoParameter("ABC")).hasId("MYECHOPARAMS2");
        assertThrows(PersistenceException.class,
                source::load);

        assertTrue(SampleRepository.getInstance().isEmpty());
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDef() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter("ABC")).hasId(MYECHOPARAMS);

        getTestService().echoParameter("ABC");
        String pathToFile = "./record/manualIdSetForRecordingAndLoadingCorrectDef.json";
        PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder(pathToFile).build());
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.echoParameter("ABC")).hasId(MYECHOPARAMS);
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals("ABC", getTestService().echoParameter("ABC"));
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDefVoidMethod() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        testServiceSampler.noReturnValue(2);
        Sample.setIdToLastMethodCall("NoReturnValue");

        getTestService().noReturnValue(2);

        String pathToFile = "./record/manualIdSetForRecordingAndLoadingCorrectDef.json";
        PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder(pathToFile).build());
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        testServiceSampler.noReturnValue(2);
        Sample.setIdToLastMethodCall("NoReturnValue");
        source.load();
        getTestService().noReturnValue(2);

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sample.verifyCallQuantity(TestService.class, new FixedQuantity(1)).noReturnValue(2);
        Files.delete(Paths.get(pathToFile));
    }
}