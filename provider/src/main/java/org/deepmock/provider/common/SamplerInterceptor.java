package org.deepmock.provider.common;

import org.deepmock.core.model.SampleDefinition;
import org.deepmock.core.model.ParameterMatcher;

import java.util.List;

/**
 * A {@link SamplerInterceptor} intercepts calls to Methods on "sampled" objects and replaces the return value and/or the behavior of the intercepted method.
 * The new return value, or the new behavior, is defined in test classes using the fluent-api defined by {@link org.deepmock.core.api.Sample}.
 *
 * There are various frameworks and architectures that require different technologies to enable method interception. To honor that,
 * DeepSampler makes usage of different {@link SamplerInterceptor}s. All {@link SamplerInterceptor}s must implement this interface.
 *
 * You may define your own {@link SamplerInterceptor} in case you are using an architecture that is not supported by default.
 */
public interface SamplerInterceptor {

    /**
     * Checks if the arguments match to the the arguments defined in sampleDefinition. The concrete test is done by a
     * {@link ParameterMatcher} that is defined through the core api.
     *
     * @param sampleDefinition A {@link SampleDefinition} that should be applied to method calls with particular arguments.
     * @param arguments the arguments of an actual method call.
     * @return Returns {@code true} if arguments match to the expected arguments of sample.
     */
    default boolean argumentsMatch(SampleDefinition sampleDefinition, Object[] arguments) {
        List<ParameterMatcher> parameterMatchers = sampleDefinition.getParameter();

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
