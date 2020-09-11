package org.deepsampler.provider.common;

import org.deepsampler.core.api.Sampler;
import org.deepsampler.core.api.Sample;
import org.deepsampler.core.error.VerifyException;
import org.deepsampler.core.internal.FixedQuantity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This Testclass must be be used to test all aop-provider in order to ensure that all providers would support the same
 * functionality.
 */
public abstract class SamplerInterceptorTest {

    public static final String VALUE_A = "Value A";
    public static final String VALUE_B = "Value B";
    public static final String VALUE_C = "Value C";
    public static final int INT_VALUE = 42;
    private static final TestBean TEST_BEAN_A = new TestBean("OneString", 42);
    private static final TestBean TEST_BEAN_B = new TestBean("AnotherString", 24);

    /**
     * The {@link TestService} is a Service that is used to test method interception by a {@link SamplerInterceptor}. Since this class must be
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


    @Test
    public void singleArgumentValueMatchesAndSampleIsChanged() {
        Sampler.clear();

        //WHEN UNCHANGED
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_A));

        // GIVEN WHEN
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        //THEN
        assertEquals(VALUE_A, getTestService().echoParameter(VALUE_B));
    }

    @Test
    public void singleArgumentValueDoesNotMatchAndSampleIsNotChanged() {
        Sampler.clear();

        // GIVEN WHEN
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        //THEN
        assertEquals(VALUE_C, getTestService().echoParameter(VALUE_C));
    }

    @Test
    public void methodHasNoSampleAndIsNotChanged() {
        Sampler.clear();

        // GIVEN WHEN
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_B)).is(VALUE_A);

        //THEN
        assertEquals(TEST_BEAN_A, getTestService().echoParameter(TEST_BEAN_A));
    }

    @Test
    public void methodWithNoParameterShouldChangeItsBehavior() {
        Sampler.clear();

        //WHEN UNCHANGED
        assertEquals(-1, getTestService().getMinusOne());

        // GIVEN WHEN
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.getMinusOne()).is(INT_VALUE);

        //THEN
        assertEquals(INT_VALUE, getTestService().getMinusOne());
    }

    @Test
    public void singleBeanArgumentValueMatchesAndSampleIsChanged() {
        Sampler.clear();

        // WHEN UNCHANGED
        assertEquals(TEST_BEAN_A, getTestService().echoParameter(TEST_BEAN_A));

        // CHANGE
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A)).is(TEST_BEAN_B);

        //THEN
        assertEquals(TEST_BEAN_B, getTestService().echoParameter(TEST_BEAN_A));
    }

    @Test
    public void deepObjectSampleIsChanged() {
        Sampler.clear();
        TestServiceContainer testServiceContainer = getTestServiceContainer();

        // WHEN UNCHANGED
        assertEquals(VALUE_C + TestServiceContainer.SUFFIX_FROM_SERVICE_CONTAINER, testServiceContainer.augmentValueFromTestService());

        // CHANGE
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(VALUE_C)).is(VALUE_B);

        //THEN
        assertEquals(VALUE_B + TestServiceContainer.SUFFIX_FROM_SERVICE_CONTAINER, testServiceContainer.augmentValueFromTestService());
    }

    @Test
    public void verifyMethodNotCalled() {
        Sampler.clear();

        // CHANGE
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A)).is(TEST_BEAN_B);

        // CALL
        getTestService().echoParameter(TEST_BEAN_B);

        //THEN
        Sample.verifyTrait(TestService.class, new FixedQuantity(0)).echoParameter(TEST_BEAN_A);
        Sample.verifyTrait(TestService.class, new FixedQuantity(0)).getMinusOne();
    }

    @Test
    public void verifyMethodCalledOnce() {
        Sampler.clear();

        // CHANGE
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A)).is(TEST_BEAN_B);

        // CALL
        getTestService().echoParameter(TEST_BEAN_A);

        //THEN
        Sample.verifyTrait(TestService.class, new FixedQuantity(1)).echoParameter(TEST_BEAN_A);
        Sample.verifyTrait(TestService.class, new FixedQuantity(0)).echoParameter(TEST_BEAN_B);
        Sample.verifyTrait(TestService.class, new FixedQuantity(0)).getMinusOne();
    }

    @Test
    public void verifyMethodCalledMultipleAndMixed() {
        Sampler.clear();

        // CHANGE
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_B)).is(TEST_BEAN_B);
        Sample.of(testServiceSampler.getMinusOne()).is(1);

        // CALL
        getTestService().echoParameter(TEST_BEAN_B);
        getTestService().echoParameter(TEST_BEAN_B);
        getTestService().getMinusOne();

        //THEN
        Sample.verifyTrait(TestService.class, new FixedQuantity(0)).echoParameter(TEST_BEAN_A);
        Sample.verifyTrait(TestService.class, new FixedQuantity(2)).echoParameter(TEST_BEAN_B);
        Sample.verifyTrait(TestService.class, new FixedQuantity(1)).getMinusOne();
    }

    @Test
    public void verifyMethodWrongNumber() {
        Sampler.clear();

        // CHANGE
        TestService testServiceSampler = Sampler.prepare(TestService.class);
        Sample.of(testServiceSampler.echoParameter(TEST_BEAN_A)).is(TEST_BEAN_B);

        // CALL
        getTestService().getMinusOne();
        getTestService().getMinusOne();

        //THEN
        assertThrows(VerifyException.class, () -> Sample.verifyTrait(TestService.class, new FixedQuantity(1))
                .getMinusOne());
    }

}