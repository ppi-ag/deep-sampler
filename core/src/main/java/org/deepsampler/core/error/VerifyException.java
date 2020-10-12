package org.deepsampler.core.error;

import org.deepsampler.core.model.SampledMethod;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class VerifyException extends BaseException {

    public VerifyException(SampledMethod sampledMethod, int expected, int actual) {
        super("The sampled method %s was " +
                "expected to get invoked %s times, " +
                "actually it got invoked %s times", sampledMethod.toString(), expected, actual);
    }

    public VerifyException(SampledMethod sampledMethod, Object[] args, int expected, int actual) {
        super("The sampled method %s called with %s was " +
                "expected to get invoked %s times, " +
                "actually it got invoked %s times", sampledMethod.toString(), formatArguments(args), expected, actual);
    }

    private static String formatArguments(Object[] args) {
        if (args == null) {
            return "(null)";
        }

        String formattedArgs = Arrays.asList(args).stream()//
            .map(Objects::toString)//
            .collect(Collectors.joining(", "));

        return "(" + formattedArgs + ")";
    }

}
