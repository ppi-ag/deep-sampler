/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
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

    public NoMatchingParametersFoundException(final SampledMethod unmatchedMethod, final Object[] args) {
        super("The method %s should be stubbed, but it has been called with unexpected parameters: %s" +
                        "Either the SampleDefinition (e.g. %s) defines wrong parameters, " +
                        "or the tested compound has changed.",
                unmatchedMethod.getMethod().toString(),
                formatArgs(args),
                formatExampleSampler(unmatchedMethod));
    }

    public NoMatchingParametersFoundException(final List<SampleDefinition> unresolvedSampleDefinitions) {
        super("The method %s shall be stubbed, but it has been called with unexpected parameters: %s" +
                        "Either the SampleDefinition (e.g. %s) defines wrong parameters, " +
                        "or the tested compound has changed. " +
                        "%s",
                unresolvedSampleDefinitions.get(0).getSampledMethod().getMethod().toString(),
                formatArgs(unresolvedSampleDefinitions.get(0).getParameterValues().toArray()),
                formatExampleSampler(unresolvedSampleDefinitions.get(0).getSampledMethod()),
                formatNumberOfUnexpectedMethodCalls(unresolvedSampleDefinitions));
    }

    /**
     * A great number of method calls with unmatched parameters can often be found, if a compound has been changed. To
     * give developers a feeling of how many refactoring work needs to be done, the number ob further unresolved
     * {@link SampleDefinition}s is added to an Exception-message.
     *
     * @param unresolvedSampleDefinitions List of {@link SampleDefinition}s, which have unmatched parameters.
     * @return A formatted message containing the count of unresolved method-calls.
     */
    private static String formatNumberOfUnexpectedMethodCalls(final List<SampleDefinition> unresolvedSampleDefinitions) {
        final int rest = unresolvedSampleDefinitions.size() - 1;
        switch (rest) {
            case 0 : return "";
            case 1 : return "There is one further method call with unmatched parameters.";
            default: return "There are " + rest + " further method calls with unmatched parameters";
        }
    }

    private static String formatArgs(final Object[] args) {
        final StringBuilder argsFormatted = new StringBuilder("\n");

        for (final Object arg : args) {
            argsFormatted.append("\t");
            argsFormatted.append(arg != null ? arg.toString() : "null");
            argsFormatted.append("\n");
        }

        return argsFormatted.toString();
    }

    private static String formatExampleSampler(final SampledMethod unmatchedMethod) {
        return String.format("Sampler.of(%s#%s(%s))",
                unmatchedMethod.getTarget().getSimpleName(),
                unmatchedMethod.getMethod().getName(),
                formatParameters(unmatchedMethod)) ;
    }

    private static String formatParameters(final SampledMethod unmatchedMethod) {
        return Arrays.stream(unmatchedMethod.getMethod().getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
    }
}
