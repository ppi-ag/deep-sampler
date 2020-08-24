package org.deepmock.core.internal.api;

import org.deepmock.core.model.*;

public class ExecutionManager {

    public static void notify(Behavior behavior) {
        getBehaviorInformation(behavior).increaseTimesInvoked();
    }

    public static void log(Behavior behavior, MethodCall actualMethodCall) {
        getBehaviorInformation(behavior).addMethodCall(actualMethodCall);
    }

    private static BehaviorExecutionInformation getBehaviorInformation(Behavior behavior) {
        ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(behavior.getJoinPoint().getTarget());

        return executionInformation.getOrCreateByBehavior(behavior);
    }

}
