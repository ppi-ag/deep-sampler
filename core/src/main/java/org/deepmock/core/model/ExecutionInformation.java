package org.deepmock.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExecutionInformation {

    private final Map<Behavior, BehaviorExecutionInformation> behaviorExecutionInformationMap = new HashMap<>();

    public BehaviorExecutionInformation getOrCreateByBehavior(Behavior behavior) {
        return behaviorExecutionInformationMap.computeIfAbsent(behavior, b -> new BehaviorExecutionInformation(0));
    }

    public Map<Behavior, BehaviorExecutionInformation> getAll() {
        return Collections.unmodifiableMap(behaviorExecutionInformationMap);
    }
}
