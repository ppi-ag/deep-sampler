package org.deepmock.provider.guice;

import com.google.inject.Guice;
import org.deepmock.core.api.Behaviors;
import org.deepmock.core.api.Personality;
import org.deepmock.provider.common.AbstractBehaviorInterceptorTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

public class BehaviorInterceptorTest extends AbstractBehaviorInterceptorTest {

    public static final String VALUE_FROM_OUTER_CLASS = " additional stuff to ensure that this method has not been changed";

    @Inject
    private TestObjectOwner testObjectOwner;


    public BehaviorInterceptorTest() {
        Guice.createInjector(new DeepMockModule()).injectMembers(this);
    }

    @Inject
    public void setTestObject(AbstractBehaviorInterceptorTest.TestObject testObject) {
        super.setTestObject(testObject);
    }

    @Test
    public void deepObjectBehaviorIsChanged() {
        Behaviors.clear();

        // WHEN UNCHANGED
        assertEquals(VALUE_C + VALUE_FROM_OUTER_CLASS, testObjectOwner.doSomeThingWithTestObject());

        // CHANGE
        TestObject changedTestObject = Behaviors.of(TestObject.class);
        Personality.hasTrait(changedTestObject.shouldChangeItsBehavior(VALUE_C)).returning(VALUE_B);

        //THEN
        assertEquals(VALUE_B + VALUE_FROM_OUTER_CLASS, testObjectOwner.doSomeThingWithTestObject());
    }

    public static class TestObjectOwner {

        @Inject
        private TestObject testObject;

        public String doSomeThingWithTestObject() {
            String valueFromTestObject = testObject.shouldChangeItsBehavior(VALUE_C);
            return valueFromTestObject + VALUE_FROM_OUTER_CLASS;
        }

    }
}