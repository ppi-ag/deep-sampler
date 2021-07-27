package de.ppi.deepsampler.persistence.error;

import de.ppi.deepsampler.core.model.SampleDefinition;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link ParametersAreNotMatchedException} is thrown if a JSON-file (or any other persistent Sample) contains
 * Samples that need parameter matchers which were not defined using the {@link de.ppi.deepsampler.core.api.Sample#of(Object)} API.
 * To be more specific: The stubbed method of a Sampler has been found, but the parameters don't fit.
 */
public class ParametersAreNotMatchedException extends PersistenceException {

    public ParametersAreNotMatchedException(Set<SampleDefinition> missedSampleDefinitions) {
        super("The following persistent Samples have a Sampler (defined using Sampler.of(...)) " +
                        "but the parameter matchers used don't fit to the persistent parameters:\n%s",
                formatErrorMessage(missedSampleDefinitions));
    }

    private static String formatErrorMessage(Set<SampleDefinition> missedSampleDefinitions) {
        return missedSampleDefinitions.stream()
                .map(ParametersAreNotMatchedException::formatSampleDefinition)
                .collect(Collectors.joining("\n\n"));
    }

    private static String formatSampleDefinition(SampleDefinition sampleDefinition) {
        String sampleDescription = sampleDefinition.getSampleId() + "\n";

        sampleDescription += sampleDefinition.getParameterValues().stream()
                .map(value -> value != null ? value.toString() : "null")
                .map(value -> "\t" + value)
                .collect(Collectors.joining("\n"));

        return sampleDescription;
    }
}
