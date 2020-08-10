package org.deepmock.core.model;

import java.util.HashMap;
import java.util.Map;

public class ExecutionInformation {

    private final Map<Behavior, BehaviorExecutionInformation> behaviorExecutionInformationMap = new HashMap<>();

    public BehaviorExecutionInformation getInformationByBehavior(Behavior behavior) {
        return behaviorExecutionInformationMap.get(behavior);
    }

    public void addInformationToBehavior(Behavior behavior, BehaviorExecutionInformation information) {
        behaviorExecutionInformationMap.put(behavior, information);
    }
}
