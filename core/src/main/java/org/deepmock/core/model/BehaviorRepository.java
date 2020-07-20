package org.deepmock.core.model;

import org.deepmock.core.error.InvalidConfigException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BehaviorRepository {

    private ThreadLocal<Map<JoinPoint, Behavior>> traits = ThreadLocal.withInitial(() -> new HashMap<>());
    private ThreadLocal<Behavior> currentBehavior = new ThreadLocal<>();

    private static BehaviorRepository myInstance;

    public static synchronized BehaviorRepository getInstance() {
        if (myInstance == null) {
            myInstance = new BehaviorRepository();
        }

        return myInstance;
    }

    public void add(Behavior behavior) {
        if (behavior.getJoinPoint() == null) {
            throw new InvalidConfigException("%s must define a %s", behavior.toString(), JoinPoint.class.getSimpleName());
        }
        setCurrentBehavior(behavior);
        traits.get().put(behavior.getJoinPoint(), behavior);
    }


    public Behavior find(JoinPoint joinPoint) {
        return traits.get().get(joinPoint);
    }

    private void setCurrentBehavior(Behavior behavior) {
        currentBehavior.set(behavior);
    }

    public Behavior getCurrentBehavior() {
        return currentBehavior.get();
    }

    public List<Behavior> getCurrentExecutionBehaviors() {
        return new ArrayList<>(traits.get().values());
    }
}
