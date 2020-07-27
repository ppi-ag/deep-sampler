package org.deepmock.core.api;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.ParameterMatcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BehaviorsTest {

    public static final String PARAMETER_VALUE = "Blubb";
    private static final Bean BEAN_A = new Bean("a", 1);
    private static final Bean BEAN_A_COPY = new Bean("a", 1);
    private static final Bean BEAN_B = new Bean("b", 2);

    @Test
    public void testBehaviorDefinitionWithoutParam() {
        // GIVEN WHEN
        Quantity quantityBehavior = Behaviors.of(Quantity.class);
        Personality.hasTrait(quantityBehavior.getTimes()).returning(4);

        // THEN
        Behavior currentBehavior = BehaviorRepository.getInstance().getCurrentBehavior();
        assertEquals(Quantity.class, currentBehavior.getJoinPoint().getTarget());
        assertTrue(currentBehavior.getParameter().isEmpty());
        assertEquals(4, currentBehavior.getReturnValueSupplier().supply());
    }

    @Test
    public void testBehaviorDefinitionWithPrimitiveParam() {
        //GIVEN WHEN
        TestObject testObjectBehavior = Behaviors.of(TestObject.class);
        Personality.hasTrait(testObjectBehavior.doSomeThing(PARAMETER_VALUE)).returning("New Behavior");

        //THEN
        Behavior currentBehavior = BehaviorRepository.getInstance().getCurrentBehavior();
        List<ParameterMatcher> parameter = currentBehavior.getParameter();

        assertEquals(parameter.size(), 1);
        assertTrue(parameter.get(0).matches(PARAMETER_VALUE));
    }

    @Test
    public void testBehaviorDefinitionWithBeanParam() {
        //GIVEN WHEN
        TestObject testObjectBehavior = Behaviors.of(TestObject.class);
        Personality.hasTrait(testObjectBehavior.doSomeThing(BEAN_A)).returning(BEAN_B);

        //THEN
        Behavior currentBehavior = BehaviorRepository.getInstance().getCurrentBehavior();
        List<ParameterMatcher> parameter = currentBehavior.getParameter();

        assertEquals(parameter.size(), 1);
        assertTrue(parameter.get(0).matches(BEAN_A_COPY));
    }

    public static class TestObject {

        public String doSomeThing(String someParameter) {
            return someParameter;
        }

        public Bean doSomeThing(Bean bean) {
            return bean;
        }
    }


    public static class Bean {
        private String someString;
        private int someInt;

        public Bean(String someString, int someInt) {
            this.someString = someString;
            this.someInt = someInt;
        }

        @Override
        public boolean equals(Object other) {
            return EqualsBuilder.reflectionEquals(this, other);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
}