/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.common;

import de.ppi.deepsampler.core.api.*;
import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.core.error.VerifyException;
import de.ppi.deepsampler.core.internal.FixedQuantity;
import de.ppi.deepsampler.core.model.ExecutionRepository;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.persistence.api.PersistentSampleManager;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;
import de.ppi.deepsampler.provider.testservices.DecoupledTestService;
import de.ppi.deepsampler.provider.testservices.DecoupledTestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static de.ppi.deepsampler.core.api.Matchers.*;
import static de.ppi.deepsampler.core.internal.FixedQuantity.*;
import static de.ppi.deepsampler.persistence.api.PersistentMatchers.combo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This TestClass must be be used to test all aop-provider in order to ensure that all providers would support the same
 * functionality.
 */
@SuppressWarnings("java:S5960")
public abstract class SamplerAspectTest {

    public static final String VALUE_A = "Value A";
    public static final String VALUE_B = "Value B";
    public static final String VALUE_C = "Value C";
    public static final int INT_VALUE = 42;
    private static final TestBean TEST_BEAN_A = new TestBean();
    private static final TestBean TEST_BEAN_B = new TestBean();
    public static final String MY_ECHO_PARAMS = "MY ECHO PARAMS";
    public static final String NO_RETURN_VALUE_SAMPLE_ID = "NoReturnValue";
    public static final byte[] SOME_BYTES = {1, 2, 3, 4};


    /**
     * The {@link TestService} is a Service that is used to test method interception by a SamplerInterceptor. Since this class must be
     * instantiated by the concrete Dependency Injection Framework, the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance of {@link TestService} that has been created in a way that enables method interception by a particular AOP-framework (i.e. Spring).
     */
    public abstract TestService getTestService();

    /**
     * The {@link FinalTestService} is a Service that is used to test if aop-providers can cope with final classes (final classes cannot be intercepted).
     * Since this class must be instantiated by the concrete Dependency Injection Framework, the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance of {@link FinalTestService} that has been created in a way that enables method interception by a particular AOP-framework (i.e. Spring).
     */
    public abstract FinalTestService getFinalTestService();

    /**
     * The {@link TestServiceContainer} delegates to {@link TestService} and is used to test deeper object trees.
     * Since this class must be instantiated by the concrete Dependency Injection Framework, the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance of {@link TestService} that has been created in a way that enables method interception by a particular AOP-framework (i.e. Spring).
     */
    public abstract TestServiceContainer getTestServiceContainer();

    /**
     * The {@link DecoupledTestService} is used to test if decoupled classes that are autowired by their interfaces can be stubbed.
     * Since this class must be instantiated by the concrete Dependency Injection Framework, the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance of {@link DecoupledTestService} that has been created in a way that enables method interception by a particular AOP-framework (i.e. Spring).
     */
    public abstract DecoupledTestService getDecoupledTestService();

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
    public void customMatcherWithWrongParameterTypeIsIgnored() {
        //WHEN UNCHANGED
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        final TestServiceContainer anotherSampler = Sampler.prepare(TestServiceContainer.class);

        Sample.of(testServiceSampler.anotherMethodThatReturnsStrings(Matchers.matcher(a -> true))).is(VALUE_B);
        Sample.of(anotherSampler.echoParameter(Matchers.matcher(a -> true))).is(TEST_BEAN_B);

        //THEN
        // The first Samplers type would fit, but the method is different, it should be ignored
        // The second Samplers Method would fit, but the type is wrong so it should be ignored
        // If either one or the other Sampler ist not ignored, Matchers.matcher() would yield a ClassCastException.
        assertEquals(TEST_BEAN_A, getTestService().echoParameter(TEST_BEAN_A));
    }

    @Test
    public void equalsMatcherComplainsWhenParameterHasNoEqualsMethod() {
        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(new TestBeanWithoutEquals())).is(new TestBeanWithoutEquals());

        assertThrows(InvalidConfigException.class, () -> getTestService().echoParameter(new TestBeanWithoutEquals()));
    }

    @Test
    public void canCopeWithNullValue() {
        //WHEN UNCHANGED
        assertNull(getTestService().echoParameter((String) null));

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        //THEN
        assertNull(getTestService().echoParameter((String) null));

        //GIVEN WHEN
        Sample.of(testServiceSampler.echoParameter((String) null)).is(VALUE_A);

        //THEN
        assertEquals(VALUE_A, getTestService().echoParameter((String) null));
    }

    @Test
    public void finalClassCannotBeStubbed() {
        // GIVEN WHEN
        FinalTestService finalTestService = getFinalTestService();

        // THEN
        assertNotNull(finalTestService);

        assertThrows(RuntimeException.class, () -> Sampler.prepare(FinalTestService.class));
    }

    @Test
    public void serviceCanBeCastedFromInterfaceToConcrete() {
        // GIVEN WHEN
        DecoupledTestService decoupledTestService = getDecoupledTestService();

        // THEN

        // The following cast is not possible if the AOP-Framework creates a Proxy based on the interface DecoupledTestService
        // instead as a subclass of DecoupledTestServiceImpl. This is the case with Spring-AOP by default. Even though up-casts,
        // like the following one, are bad smelling code, we expect them to occur frequently. So DeepSampler must cope with it.
        // To enable this, we have to exclude classes from being intercepted by adding a proper Pointcut expression to our
        // Spring-Aspect.
        DecoupledTestServiceImpl implementation = (DecoupledTestServiceImpl) decoupledTestService;
        assertNotNull(implementation);
    }




    @Test
    public void multipleSamplersAreHandledDistinctly() {
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
    public void samplerCanReturnArrays() {
        // WHEN UNCHANGED
        assertEquals(1, getTestService().getArrayOfTestBeans().length);

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getArrayOfTestBeans()).is(new TestBean[]{new TestBean(), new TestBean()});

        // THEN
        assertEquals(2, getTestService().getArrayOfTestBeans().length);
    }


    @Test
    public void verifyMethodNotCalled() {
        // CHANGE
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(sameAs(TEST_BEAN_A))).is(TEST_BEAN_B);

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
        Sample.of(testServiceSampler.echoParameter(sameAs(TEST_BEAN_A))).is(TEST_BEAN_B);

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
        Sample.of(testServiceSampler.echoParameter(sameAs(TEST_BEAN_B))).is(TEST_BEAN_B);
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
        Sample.of(testServiceSampler.echoParameter(sameAs(TEST_BEAN_B)));
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
    public void exceptionCanBeThrownByStub() throws TestException {
        //WHEN UNCHANGED
        assertDoesNotThrow(() -> getTestService().throwsException());

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.throwsException()).throwsException(Exception.class);

        //THEN
        assertThrows(Exception.class, () -> getTestService().throwsException());
    }

    @Test
    public void runtimeExceptionCanBeThrownByStub() {
        //WHEN UNCHANGED
        assertDoesNotThrow(() -> getTestService().getMinusOne());

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getMinusOne()).throwsException(new RuntimeException());

        //THEN
        assertThrows(RuntimeException.class, () -> getTestService().getMinusOne());
    }

    @Test
    public void exceptionCanBeThrownByVoidStub() {
        //WHEN UNCHANGED
        assertDoesNotThrow(() -> getTestService().voidThrowsException());

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler::voidThrowsException).throwsException(Exception.class);

        //THEN
        assertThrows(Exception.class, () -> getTestService().voidThrowsException());
    }

    @Test
    public void runtimeExceptionCanBeThrownBVoidStub() {
        //WHEN UNCHANGED
        assertDoesNotThrow(() -> getTestService().voidThrowsException());

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler::voidThrowsException).throwsException(new RuntimeException());

        //THEN
        assertThrows(RuntimeException.class, () -> getTestService().voidThrowsException());
    }

    @Test
    public void voidMethodCanBeDeactivated() {
        //WHEN UNCHANGED
        getTestService().setCounter(0);
        assertEquals(0, getTestService().getCounter());
        getTestService().incrementCounter();
        assertEquals(1, getTestService().getCounter());

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler::incrementCounter).doesNothing();

        //THEN
        getTestService().incrementCounter();
        assertEquals(1, getTestService().getCounter());
    }

    @Test
    void behaviorOfVoidMethodCanBeChanged() {
        //WHEN UNCHANGED
        assertEquals(0, getTestService().getCounter());
        getTestService().incrementCounter();
        assertEquals(1, getTestService().getCounter());

        // GIVEN WHEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);

        Sample.of(testServiceSampler::incrementCounter).answers(stubMethodInvocation -> {
            final TestService testService = stubMethodInvocation.getStubInstance();
            testService.setCounter(100);
        });

        //THEN
        getTestService().incrementCounter();
        assertEquals(100, getTestService().getCounter());
    }

    @Test
    public void samplesCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_A));
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A));

        getTestService().echoParameter(VALUE_A);
        getTestService().echoParameter(TEST_BEAN_A);
        final String pathToFile = "./record/samplesCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.echoParameter(VALUE_A));
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertNotNull(getTestService().echoParameter(VALUE_A));
        assertNotNull(getTestService().echoParameter(TEST_BEAN_A));
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void byteArrayCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(any(TestBeanWithBytes.class)));

        getTestService().echoParameter(new TestBeanWithBytes(SOME_BYTES));
        final String pathToFile = "./record/byteArrayCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.echoParameter(any(TestBeanWithBytes.class)));
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertArrayEquals(SOME_BYTES , getTestService().echoParameter(new TestBeanWithBytes(SOME_BYTES)).getSomeBytes());
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void voidMethodsCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

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
        Sampler.clear();

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
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getListOfTestBeans());

        getTestService().getListOfTestBeans();
        final String pathToFile = "./record/listOfTestBeansReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getListOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getListOfTestBeans().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getListOfTestBeans().get(0).getValue());

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void listOfStringsReturnValueCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getListOfStrings());

        getTestService().getListOfStrings();
        final String pathToFile = "./record/listOfStringsReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getListOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getListOfStrings().size());

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void setOfStringsReturnValueCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getSetOfStrings());

        getTestService().getSetOfStrings();
        final String pathToFile = "./record/setOfStringsReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getSetOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getSetOfStrings().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getSetOfStrings().toArray()[0]);

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void setOfTestBeansReturnValueCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getSetOfTestBeans());

        getTestService().getSetOfTestBeans();
        final String pathToFile = "./record/setOfTestBeansReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getSetOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getSetOfTestBeans().size());
        TestBean testBean = getTestService().getSetOfTestBeans().stream().findFirst().get();
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, testBean.getValue());

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void arrayOfTestBeansReturnValueCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getArrayOfTestBeans());

        getTestService().getArrayOfTestBeans();
        final String pathToFile = "./record/arrayOfTestBeansReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getArrayOfTestBeans());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getArrayOfTestBeans().length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getArrayOfTestBeans()[0].getValue());


        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void arrayOfStringsReturnValueCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getArrayOfStrings());

        getTestService().getArrayOfStrings();
        final String pathToFile = "./record/arrayOfStringsReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getArrayOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getArrayOfStrings().length);
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getArrayOfStrings()[0]);

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    void multidimensionalArrayOfStringsCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getArrayOfStrings2d());

        getTestService().getArrayOfStrings2d();
        final String pathToFile = "./record/multidimensionalArrayCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getArrayOfStrings2d());
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
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getArrayOfTestBeans3d());

        getTestService().getArrayOfTestBeans3d();
        final String pathToFile = "./record/multidimensionalArrayOfTestBeansCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getArrayOfTestBeans3d());
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
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getMapOfStrings());

        getTestService().getMapOfStrings();
        final String pathToFile = "./record/mapOfStringsReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getMapOfStrings());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfStrings().size());
        assertEquals(TestService.HARD_CODED_RETURN_VALUE, getTestService().getMapOfStrings().get(TestService.HARD_CODED_RETURN_VALUE));

        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void mapOfStringsToTestBeansReturnValueCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getMapOfStringsToTestBeans());

        getTestService().getMapOfStringsToTestBeans();
        final String pathToFile = "./record/mapOfStringsToTestBeansReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getMapOfStringsToTestBeans());
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
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getMapOfIntegers());

        getTestService().getMapOfIntegers();
        final String pathToFile = "./record/mapOfIntegersReturnValueCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.getMapOfIntegers());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(1, getTestService().getMapOfIntegers().size());
        assertEquals(2, getTestService().getMapOfIntegers().get(1));

        Files.delete(Paths.get(pathToFile));
    }



    @Test
    public void manualIdSetForRecordingAndLoadingNoCorrectDef() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final String pathToFile = "./record/manualIdSetForRecordingAndLoading.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.echoParameter("ABC")).hasId("MY WRONG ECHO PARAMS");
        assertThrows(PersistenceException.class,
                source::load);

        assertTrue(SampleRepository.getInstance().isEmpty());
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void manualIdSetForRecordingAndLoadingCorrectDef() throws IOException {
        Sampler.clear();

        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final String pathToFile = "./record/manualIdSetForRecordingAndLoadingCorrectDef.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.echoParameter("ABC")).hasId(MY_ECHO_PARAMS);
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
        Sample.setIdToLastMethodCall(NO_RETURN_VALUE_SAMPLE_ID);

        getTestService().noReturnValue(2);

        final String pathToFile = "./record/manualIdSetForRecordingAndLoadingCorrectDef.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        testServiceSampler.noReturnValue(2);
        Sample.setIdToLastMethodCall(NO_RETURN_VALUE_SAMPLE_ID);
        source.load();
        getTestService().noReturnValue(2);

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(NO_RETURN_VALUE_SAMPLE_ID, SampleRepository.getInstance().getSamples().get(0).getSampleId());
        Sample.verifyCallQuantity(TestService.class, new FixedQuantity(1)).noReturnValue(2);
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void localDateTimeCanBeRecordedAndLoaded() throws IOException {
        Sampler.clear();
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.testLocalDateTime());

        getTestService().testLocalDateTime();

        final String pathToFile = "./record/localDateTimeCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        Sample.of(testServiceSampler.testLocalDateTime());
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(LocalDateTime.of(2020, 10, 29, 10, 10, 10), getTestService().testLocalDateTime());
        Files.delete(Paths.get(pathToFile));
    }

    @Test
    public void threadScopeWorks() throws ExecutionException, InterruptedException {
        Sampler.clear();

        // WHEN UNCHANGED
        assertTrue(SampleRepository.getInstance().getSamples().isEmpty());
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));

        // GIVEN
        Execution.setScope(ScopeType.THREAD);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // WHEN

        Future<?> createsASampler = executorService.submit(() -> {
            final TestService testServiceSampler = Sampler.prepare(TestService.class);
            Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

            assertEquals(VALUE_A, getTestService().echoParameter(VALUE_B));
            assertFalse(ExecutionRepository.getInstance().getOrCreate(TestService.class).getAll().isEmpty());
        });

        Future<?> findsNoSampler = executorService.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // THEN
            assertEquals(VALUE_B, getTestService().echoParameter(VALUE_B));
            assertTrue(ExecutionRepository.getInstance().getOrCreate(TestService.class).getAll().isEmpty());
        });

        createsASampler.get();
        findsNoSampler.get();

        // THEN
        assertEquals(VALUE_B, getTestService().echoParameter(VALUE_B));
        assertTrue(ExecutionRepository.getInstance().getOrCreate(TestService.class).getAll().isEmpty());
    }

    @Test
    public void singletonScopeWorks() throws ExecutionException, InterruptedException {
        Sampler.clear();

        // WHEN UNCHANGED
        assertTrue(SampleRepository.getInstance().getSamples().isEmpty());
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));

        // GIVEN
        Execution.setScope(ScopeType.SINGLETON);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        //WHEN

        Future<?> createsASampler = executorService.submit(() -> {
            final TestService testServiceSampler = Sampler.prepare(TestService.class);
            Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

            assertEquals(VALUE_A, getTestService().echoParameter(VALUE_B));
            assertFalse(ExecutionRepository.getInstance().getOrCreate(TestService.class).getAll().isEmpty());
        });

        Future<?> findsNoSampler = executorService.submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // THEN
            assertEquals(VALUE_A, getTestService().echoParameter(VALUE_B));
            assertFalse(ExecutionRepository.getInstance().getOrCreate(TestService.class).getAll().isEmpty());
        });

        createsASampler.get();
        findsNoSampler.get();

        // THEN
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_B));
        assertFalse(ExecutionRepository.getInstance().getOrCreate(TestService.class).getAll().isEmpty());
    }

    @Test
    void testPostProcessSampleReturnGlobal() {
        // GIVEN
        Sampler.clear();
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_A)).is(VALUE_B);
        Sample.of(testServiceSampler.testLocalDateTime()).is(LocalDateTime.MIN);
        Execution.useGlobal((a, b, c) -> null);

        // WHEN
        String resultEcho = getTestService().echoParameter(VALUE_A);
        LocalDateTime localDateTime = getTestService().testLocalDateTime();

        // THEN
        assertNull(resultEcho);
        assertNull(localDateTime);
    }

    @Test
    void testPostProcessSampleReturnLocal() {
        // GIVEN
        Sampler.clear();
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_A)).is(VALUE_B);
        Sample.of(testServiceSampler.testLocalDateTime()).is(LocalDateTime.MIN);
        Execution.useForLastSample((a, b, c) -> {
            // THEN
            assertEquals("public java.time.LocalDateTime de.ppi.deepsampler.provider.common.TestService.testLocalDateTime()", a.getSampleId());
            assertEquals(getTestService(), b.getStubInstance());
            assertEquals(LocalDateTime.MIN, c);
            return null;
        });

        // WHEN
        String resultEcho = getTestService().echoParameter(VALUE_A);
        LocalDateTime localDateTime = getTestService().testLocalDateTime();

        // THEN
        assertEquals(VALUE_B, resultEcho);
        assertNull(localDateTime);
    }

    @Test
    void testComboMatcherLoadAllButAcceptOnlyA() throws IOException {
        // GIVEN
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().echoParameter("ABC");
        final String pathToFile = "./record/comboMatcherSingleArgument.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();
        Sampler.clear();
        Sample.of(testServiceSampler.echoParameter(combo(anyString(), (f, s) -> f.equals("A")))).hasId(MY_ECHO_PARAMS);

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
        Sample.of(testServiceSampler.methodWithThreeParametersReturningLast(anyString(), anyString(), anyString())).hasId(MY_ECHO_PARAMS);

        getTestService().methodWithThreeParametersReturningLast("BLOCK", "B", "R1");
        getTestService().methodWithThreeParametersReturningLast("NOBLOCK", "A", "R2");
        getTestService().methodWithThreeParametersReturningLast("BLOCK", "C", "R3");
        final String pathToFile = "./record/comboMatcherTwoArguments.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();
        Sampler.clear();
        Sample.of(testServiceSampler.methodWithThreeParametersReturningLast(equalTo("BLOCK"), combo(anyString(), (f, s) -> f.equals("B")), combo(anyString(), (f, s) -> true))).hasId(MY_ECHO_PARAMS);

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
        Sampler.clear();
        final TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.testLocalDateTime());

        getTestService().testLocalDateTime();

        final String pathToFile = "./record/localDateTimeCanBeRecordedAndLoaded.json";
        final PersistentSampleManager source = PersistentSampler.source(JsonSourceManager.builder().buildWithFile(pathToFile));
        source.record();

        assertFalse(SampleRepository.getInstance().isEmpty());
        Sampler.clear();
        assertTrue(SampleRepository.getInstance().isEmpty());

        // MIX persistence definition and pure java definition
        Sample.of(testServiceSampler.testLocalDateTime());
        Sample.of(testServiceSampler.echoParameter("ABC")).is("CBD");
        source.load();

        assertFalse(SampleRepository.getInstance().isEmpty());
        assertEquals(LocalDateTime.of(2020, 10, 29, 10, 10, 10), getTestService().testLocalDateTime());
        assertEquals("CBD", getTestService().echoParameter("ABC"));
        Files.delete(Paths.get(pathToFile));
    }

}