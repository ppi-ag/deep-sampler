package org.deepmock.core.api;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.ParameterMatcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BehaviorsTest {

    public static final String PARAMETER_VALUE = "Blubb";

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
    public void testBehaviorDefinitionWithParam() {
        //GIVEN WHEN
        TestObject testObjectBehavior = Behaviors.of(TestObject.class);
        Personality.hasTrait(testObjectBehavior.doSomeThing(PARAMETER_VALUE)).returning("New Behavior");

        //THEN
        Behavior currentBehavior = BehaviorRepository.getInstance().getCurrentBehavior();
        List<ParameterMatcher> parameter = currentBehavior.getParameter();

        assertEquals(parameter.size(), 1);
        assertTrue(parameter.get(0).matches(PARAMETER_VALUE));
    }

    public static class TestObject {

        public String doSomeThing(String someParameter) {
            return someParameter;
        }
    }
}