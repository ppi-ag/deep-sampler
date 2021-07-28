package de.ppi.deepsampler.core.error;

import de.ppi.deepsampler.core.model.SampledMethod;

import java.util.Arrays;
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
                        "Either the SampleDefinition (i.g. %s) defines wrong parameter, " +
                        "or the tested compound has changed.",
                unmatchedMethod.getMethod().toString(),
                formatArgs(args),
                formatExampleSampler(unmatchedMethod));
    }

    private static String formatArgs(Object[] args) {
        String argsFormatted = "\n";

        for (Object arg : args) {
            argsFormatted += "\t";
            argsFormatted += arg != null ? arg.toString() : "null";
            argsFormatted += "\n";
        }

        return argsFormatted;
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
