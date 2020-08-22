package org.deepmock.core.internal.api;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.ExecutionInformation;
import org.deepmock.core.model.ExecutionRepository;

public class ExecutionManager {

    public static void notify(Behavior behavior) {
        ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(behavior.getJoinPoint().getTarget()
        );
        executionInformation.getOrCreateByBehavior(behavior).increaseTimesInvoked();
    }

}
