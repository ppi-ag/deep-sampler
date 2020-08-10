package org.deepmock.core.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BehaviorRepositoryTest {

    @Test
    public void behaviorIsFound() throws NoSuchMethodException {
        // GIVEN
        JoinPoint registeredJoinPoint = createJoinPoint(TestObject.class);
        List<ParameterMatcher> registeredParameter = Arrays.asList(parameter -> parameter.equals("Argument"));

        Behavior registeredBehavior = new Behavior(registeredJoinPoint);
        registeredBehavior.setParameter(registeredParameter);
        registeredBehavior.setReturnValueSupplier(() -> "ReturnValue");

        // WHEN
        BehaviorRepository.getInstance().add(registeredBehavior);
        JoinPoint foundJoinPoint = createJoinPoint(TestObject.class);

        // THEN
        Behavior expectedBehavior = BehaviorRepository.getInstance().find(foundJoinPoint);
        assertNotNull(expectedBehavior);
    }

    @Test
    public void behaviorIsFoundOnSuperClass() throws NoSuchMethodException {
        // GIVEN
        JoinPoint registeredJoinPoint = createJoinPoint(TestObject.class);
        List<ParameterMatcher> registeredParameter = Arrays.asList(parameter -> parameter.equals("Argument"));

        Behavior registeredBehavior = new Behavior(registeredJoinPoint);
        registeredBehavior.setParameter(registeredParameter);
        registeredBehavior.setReturnValueSupplier(() -> "ReturnValue");

        // WHEN
        BehaviorRepository.getInstance().add(registeredBehavior);
        JoinPoint foundJoinPoint = createJoinPoint(TestSubObject.class);

        // THEN
        Behavior expectedBehavior = BehaviorRepository.getInstance().find(foundJoinPoint);
        assertNotNull(expectedBehavior);
    }

    private JoinPoint createJoinPoint(Class<?> joinPointClass) throws NoSuchMethodException {
        Method joinPointMethod = joinPointClass.getMethod("someMethod", String.class);
        return new JoinPoint(joinPointClass, joinPointMethod);
    }

    private static class TestObject {
        public String someMethod(String parameter) {
            return parameter;
        }
    }

    private static class TestSubObject extends TestObject {

    }


}