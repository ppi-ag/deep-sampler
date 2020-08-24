package org.deepmock.provider.common;

import org.deepmock.core.api.Behaviors;
import org.deepmock.core.api.Personality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This Testclass must be be used to test all aop-provider in order to ensure that all provider support the same
 * functionality.
 */
public abstract class BehaviorInterceptorTest {

    public static final String VALUE_A = "Value A";
    public static final String VALUE_B = "Value B";
    public static final String VALUE_C = "Value C";
    public static final int INT_VALUE = 42;
    private static final TestBean TEST_BEAN_A = new TestBean("OneString", 42);
    private static final TestBean TEST_BEAN_B = new TestBean("AnotherString", 24);

    /**
     * The {@link TestService} is a Service that is used to test method interception by a {@link BehaviorInterceptor}. Since this class must be
     * instantiated by the concrete Dependency Injection Framework the createion of this instance must be done by the concrete TestCase.
     *
     * @return An instance fo {@link TestService} that has been created in a way that enables method interception by a particular framework (i.e. Spring).
     */
    public abstract TestService getTestService();

    /**
     * The {@link TestServiceContainer} delegateds to {@link TestService} and is used to test deeper object trees.
     * Since this class must be instantiated by the concrete Dependency Injection Framework the creation of this instance must be done by the concrete TestCase.
     *
     * @return An instance fo {@link TestService} that has been created in a way that enables method interception by a particular framework (i.e. Spring).
     */
    public abstract TestServiceContainer getTestServiceContainer();


    @Test
    public void singleArgumentValueMatchesAndBehaviorIsChanged() {
        Behaviors.clear();

        //WHEN UNCHANGED
        assertEquals(VALUE_A, getTestService().shouldChangeItsBehavior(VALUE_A));

        // GIVEN WHEN
        TestService changedTestService = Behaviors.of(TestService.class);
        Personality.hasTrait(changedTestService.shouldChangeItsBehavior(VALUE_B)).returning(VALUE_A);

        //THEN
        assertEquals(VALUE_A, getTestService().shouldChangeItsBehavior(VALUE_B));
    }

    @Test
    public void singleArgumentValueDoesNotMatchAndBehaviorIsNotChanged() {
        Behaviors.clear();

        // GIVEN WHEN
        TestService changedTestService = Behaviors.of(TestService.class);
        Personality.hasTrait(changedTestService.shouldChangeItsBehavior(VALUE_B)).returning(VALUE_A);

        //THEN
        assertEquals(VALUE_C, getTestService().shouldNotChangeItsBehavior(VALUE_C));
    }

    @Test
    public void methodHasNoBehaviorAndIsNotChanged() {
        Behaviors.clear();

        // GIVEN WHEN
        TestService changedTestService = Behaviors.of(TestService.class);
        Personality.hasTrait(changedTestService.shouldChangeItsBehavior(VALUE_B)).returning(VALUE_A);

        //THEN
        assertEquals(VALUE_B, getTestService().shouldNotChangeItsBehavior(VALUE_B));
    }

    @Test
    public void methodWithNoParameterShouldChangeItsBehavior() {
        Behaviors.clear();

        //WHEN UNCHANGED
        assertEquals(-1, getTestService().shouldChangeItsBehavior());

        // GIVEN WHEN
        TestService changedTestService = Behaviors.of(TestService.class);
        Personality.hasTrait(changedTestService.shouldChangeItsBehavior()).returning(INT_VALUE);

        //THEN
        assertEquals(INT_VALUE, getTestService().shouldChangeItsBehavior());
    }

    @Test
    public void singleBeanArgumentValueMatchesAndBehaviorIsChanged() {
        Behaviors.clear();

        // WHEN UNCHANGED
        assertEquals(TEST_BEAN_A, getTestService().shouldChangeItsBehavior(TEST_BEAN_A));

        // CHANGE
        TestService changedTestService = Behaviors.of(TestService.class);
        Personality.hasTrait(changedTestService.shouldChangeItsBehavior(TEST_BEAN_A)).returning(TEST_BEAN_B);

        //THEN
        assertEquals(TEST_BEAN_B, getTestService().shouldChangeItsBehavior(TEST_BEAN_A));
    }

    @Test
    public void deepObjectBehaviorIsChanged() {
        Behaviors.clear();
        TestServiceContainer testObjectServiceContainer = getTestServiceContainer();


        // WHEN UNCHANGED
        assertEquals(VALUE_C + TestServiceContainer.SUFFIX_FROM_SERVICE_CONTAINER, testObjectServiceContainer.doSomeThingWithTestObject());

        // CHANGE
        TestService changedTestService = Behaviors.of(TestService.class);
        Personality.hasTrait(changedTestService.shouldChangeItsBehavior(VALUE_C)).returning(VALUE_B);

        //THEN
        assertEquals(VALUE_B + TestServiceContainer.SUFFIX_FROM_SERVICE_CONTAINER, testObjectServiceContainer.doSomeThingWithTestObject());
    }

}