package org.deepmock.provider.common;

import org.deepmock.core.api.Behaviors;
import org.deepmock.core.api.Personality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This Testclass must be be used to test all aop-provider in order to ensure that all provider support the same
 * functionality.
 */
public abstract class AbstractBehaviorInterceptorTest {

    public static final String VALUE_A = "Value A";
    public static final String VALUE_B = "Value B";
    public static final String VALUE_C = "Value C";
    public static final int INT_VALUE = 42;
    private static final Bean BEAN_A = new Bean("OneString", 42);
    private static final Bean BEAN_B = new Bean("AnotherString", 24);


    private TestObject testObject;

    public TestObject getTestObject() {
        return testObject;
    }

    public void setTestObject(TestObject testObject) {
        this.testObject = testObject;
    }

    @Test
    public void singleArgumentValueMatchesAndBehaviorIsChanged() {
        Behaviors.clear();

        // GIVEN WHEN
        TestObject changedTestObject = Behaviors.of(TestObject.class);
        Personality.hasTrait(changedTestObject.shouldChangeItsBehavior(VALUE_B)).returning(VALUE_A);

        //THEN
        assertEquals(VALUE_A, testObject.shouldChangeItsBehavior(VALUE_B));
    }

    @Test
    public void singleArgumentValueDoesNotMachAndBehaviorIsNotChanged() {
        Behaviors.clear();

        // GIVEN WHEN
        TestObject changedTestObject = Behaviors.of(TestObject.class);
        Personality.hasTrait(changedTestObject.shouldChangeItsBehavior(VALUE_B)).returning(VALUE_A);

        //THEN
        assertEquals(VALUE_C, testObject.shouldNotChangeItsBehavior(VALUE_C));
    }

    @Test
    public void methodHasNoBehaviorAndIsNotChanged() {
        Behaviors.clear();

        // GIVEN WHEN
        TestObject changedTestObject = Behaviors.of(TestObject.class);
        Personality.hasTrait(changedTestObject.shouldChangeItsBehavior(VALUE_B)).returning(VALUE_A);

        //THEN
        assertEquals(VALUE_B, testObject.shouldNotChangeItsBehavior(VALUE_B));
    }

    @Test
    public void methodWithNoParameterShouldChangeItsBehavior() {
        Behaviors.clear();

        // GIVEN WHEN
        TestObject changedTestObject = Behaviors.of(TestObject.class);
        Personality.hasTrait(changedTestObject.shouldChangeItsBehavior()).returning(INT_VALUE);

        //THEN
        assertEquals(INT_VALUE, testObject.shouldChangeItsBehavior());
    }

    @Test
    public void singleBeanArgumentValueMatchesAndBehaviorIsChanged() {
        Behaviors.clear();

        // WHEN UNCHANGED
        assertEquals(BEAN_A, testObject.shouldChangeItsBehavior(BEAN_A));

        // CHANGE
        TestObject changedTestObject = Behaviors.of(TestObject.class);
        Personality.hasTrait(changedTestObject.shouldChangeItsBehavior(BEAN_A)).returning(BEAN_B);

        //THEN
        assertEquals(BEAN_B, testObject.shouldChangeItsBehavior(BEAN_A));
    }


    public static class TestObject {
        public String shouldChangeItsBehavior(String param) {
            return param;
        }

        public String shouldNotChangeItsBehavior(String param) {
            return param;
        }

        public int shouldChangeItsBehavior() {
            return -1;
        }

        public Bean shouldChangeItsBehavior(Bean someObject) {
            return someObject;
        }
    }


    public static class Bean {
        private String someString;
        private int someInt;

        public Bean(String someString, int someInt) {
            this.someString = someString;
            this.someInt = someInt;
        }
    }



}