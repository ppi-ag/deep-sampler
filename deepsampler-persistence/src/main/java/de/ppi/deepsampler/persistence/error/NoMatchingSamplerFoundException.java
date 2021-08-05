package de.ppi.deepsampler.persistence.error;

import com.sun.tools.jdeps.JdepsFilter;
import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.internal.FuzzySearchUtility;
import de.ppi.deepsampler.core.model.SampleDefinition;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link NoMatchingSamplerFoundException} is thrown if a JSON file (or any other source of persistent Samples)
 * contains Samples that don't have a matching Sampler. A Sampler is defined using {@link de.ppi.deepsampler.core.api.Sample#of(Object)}.
 *
 * A typical cause of this Exception is, that the signature of a sampled method has changed. Maybe the method name was refactored.
 * Since the complete method signature is saved in the JSON (or any other persistent medium) by default, the signature in the file might
 * not correspond to the refactored one in the code anymore.
 *
 * You can prevent situations like this, by defining the id of a Sample manually using Sampler.of().hasId("MyId").
 * See {@link de.ppi.deepsampler.core.api.SampleBuilder#hasId(String)}.
 */
public class NoMatchingSamplerFoundException extends PersistenceException {

    public NoMatchingSamplerFoundException(String unusedSamplerId, List<SampleDefinition> definedSampleDefinitions) {
        super("The persistent Sample with the id '%s' doesn't have a corresponding Sampler. %s" +
                "Please define a Sampler using Sampler.of(...)", unusedSamplerId, guessCorrectSampler(unusedSamplerId, definedSampleDefinitions));
    }

    public NoMatchingSamplerFoundException(Collection<String> unusedSamplerIds, List<SampleDefinition> definedSampleDefinitions) {
        super("The following persistent Samples don't have a corresponding Sampler. " +
                "Please define a Sampler using Sampler.of(...):\n%s", formatMissingSamplerIds(unusedSamplerIds, definedSampleDefinitions));
    }

    private static String formatMissingSamplerIds(Collection<String> unusedSamplerIds, List<SampleDefinition> definedSampleDefinitions) {
        return unusedSamplerIds.stream()
                .map(id -> "\t" + id + "\n\t\t" + guessCorrectSampler(id, definedSampleDefinitions))
                .collect(Collectors.joining("\n"));
    }

    private static String guessCorrectSampler(String unusedSamplerId, List<SampleDefinition> definedSampleDefinitions) {
        List<String> definedSampleIds = definedSampleDefinitions.stream().map(SampleDefinition::getSampleId).collect(Collectors.toList());
        FuzzySearchUtility.Match match = FuzzySearchUtility.findClosestString(unusedSamplerId, definedSampleIds);

        if (match != null && match.getEquality() > 0.5) {
            return "did you mean " + match.getMatchedString() + "?";
        }

        return "";
    }
}
