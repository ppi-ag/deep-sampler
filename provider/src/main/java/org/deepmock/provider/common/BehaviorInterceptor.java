package org.deepmock.provider.common;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.ParameterMatcher;

import java.util.List;

/**
 * A BehaviorInterceptor intercepts calls to Methods that should be changed during testing. The calls are delegated to
 * Behaviors that are defined in test cases and those Behaviors override the default implementation of the intercepted Methods.
 *
 * There are various frameworks and architectures that require different technologies to enable method interception. To honor that,
 * DeepMock makes usages of different BehaviorInterceptors. All BehaviorInterceptors must implement this parent class.
 *
 * You may define your own BehaviorInterceptor in case you are using an architecture that is not supported by default.
 */
public interface BehaviorInterceptor {

    /**
     * Checks if the arguments match to the the arguments defined in behavior. The concrete test is done by a
     * {@link ParameterMatcher} that will be defined through the core api.
     *
     * @param behavior A behavior that should be applied to method calls with certain arguments.
     * @param arguments the arguments of an actual method call.
     * @return Returns {@code true} if arguments match to the expected arguments of behavior.
     */
    default boolean argumentsMatch(Behavior behavior, Object[] arguments) {
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
}
