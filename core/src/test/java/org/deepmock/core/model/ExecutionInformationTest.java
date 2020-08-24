package org.deepmock.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionInformationTest {

    @Test
    void testGetOrCreateByBehavior() throws NoSuchMethodException {
        // GIVEN
        ExecutionInformation executionInformation = new ExecutionInformation();
        Behavior behavior = new Behavior(new JoinPoint(getClass(), getClass().getMethod("toString")));

        // WHEN
        BehaviorExecutionInformation behaviorExecutionInformation = executionInformation.getOrCreateByBehavior(behavior);

        // THEN
        assertTrue(behaviorExecutionInformation == executionInformation.getOrCreateByBehavior(behavior));
    }
}