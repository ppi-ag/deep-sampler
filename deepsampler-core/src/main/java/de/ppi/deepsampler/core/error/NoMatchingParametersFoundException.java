/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.error;

import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampledMethod;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Sample is defined by the method that should be stubbed and parameters, or parameter matchers. The sample value is returned if the
 * parameters, that actually have been passed to the stubbed method, match to the parameters, which have been defined in the sample definition.
 *
 * If DeepSampler notices, that a called method should be stubbed, but the parameters don't match to the Sampler, DeepSampler
 * throws this Exception to prevent unnoticed calls to the original methods.
 */
public class NoMatchingParametersFoundException extends BaseException {

    public NoMatchingParametersFoundException(SampledMethod unmatchedMethod, Object[] args) {
        super("The method %s should be stubbed, but it has been called with unexpected parameters: %s" +
                        "Either the SampleDefinition (e.g. %s) defines wrong parameters, " +
                        "or the tested compound has changed.",
                unmatchedMethod.getMethod().toString(),
                formatArgs(args),
                formatExampleSampler(unmatchedMethod));
    }

    public NoMatchingParametersFoundException(List<SampleDefinition> sampleDefinitions) {
        super("The method %s should be stubbed, but it has been called with unexpected parameters: %s" +
                        "Either the SampleDefinition (e.g. %s) defines wrong parameters, " +
                        "or the tested compound has changed.\"" +
                        "There are %d further issues.",
                sampleDefinitions.get(0).getSampledMethod().getMethod().toString(),
                formatArgs(sampleDefinitions.get(0).getParameterValues().toArray()),
                formatExampleSampler(sampleDefinitions.get(0).getSampledMethod()),
                sampleDefinitions.size() - 1);
    }

    private static String formatArgs(Object[] args) {
        StringBuilder argsFormatted = new StringBuilder("\n");

        for (Object arg : args) {
            argsFormatted.append("\t");
            argsFormatted.append(arg != null ? arg.toString() : "null");
            argsFormatted.append("\n");
        }

        return argsFormatted.toString();
    }

    private static String formatExampleSampler(SampledMethod unmatchedMethod) {
        return String.format("Sampler.of(%s#%s(%s))",
                unmatchedMethod.getTarget().getSimpleName(),
                unmatchedMethod.getMethod().getName(),
                formatParameters(unmatchedMethod)) ;
    }

    private static String formatParameters(SampledMethod unmatchedMethod) {
        return Arrays.stream(unmatchedMethod.getMethod().getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
    }
}
