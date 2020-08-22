package org.deepmock.core.model;

import org.deepmock.core.error.InvalidConfigException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.swing.UIManager.get;

public class BehaviorRepository {

    private ThreadLocal<List<Behavior>> traits = ThreadLocal.withInitial(() -> new ArrayList<>());
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
        traits.get().add(behavior);
    }


    public Behavior find(JoinPoint wantedJoinPoint, Object... args) {
        List<Behavior> behaviors = traits.get();

        for (Behavior behavior : behaviors) {
            JoinPoint joinPoint = behavior.getJoinPoint();
            boolean classMatches = joinPoint.getTarget().isAssignableFrom(wantedJoinPoint.getTarget());
            boolean methodMatches = joinPoint.getMethod().equals(wantedJoinPoint.getMethod());
            boolean argumentsMatches = argumentsMatch(behavior, args);

            if (classMatches && methodMatches && argumentsMatches) {
                return behavior;
            }
        }

        return null;
    }

    private boolean argumentsMatch(Behavior behavior, Object[] arguments) {
        List<ParameterMatcher> parameterMatchers = behavior.getParameter();

        if (parameterMatchers.size() != arguments.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            if (!parameterMatchers.get(0).matches(arguments[i])) {
                return false;
            }
        }

        return true;
    }

    private void setCurrentBehavior(Behavior behavior) {
        currentBehavior.set(behavior);
    }

    public Behavior getCurrentBehavior() {
        return currentBehavior.get();
    }

    public List<Behavior> getCurrentExecutionBehaviors() {
        return traits.get();
    }

    public void clear() {
        traits.get().clear();
    }
}
