package de.ppi.deepsampler.persistence.error;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * The {@link NoMatchingSamplerFoundException} is thrown if a JSON file (or any other source of persistent Samples)
 * contains Samples that don't have a matching Sampler. A Sampler is defined using {@link de.ppi.deepsampler.core.api.Sample#of(Object)}.
 *
 * A typical cause for this Exception is, that the signature of a sampled method has changed. Maybe the method name was refactored.
 * Since the complete method signature is saved in the JSON (or any other persistent medium) by default, the signature in the file doesn't
 * correspond to the refactored on in the code.
 *
 * You can prevent situations like this by defining the id of a Sample manually using Sampler.of().hasId("MyId").
 * See {@link de.ppi.deepsampler.core.api.SampleBuilder#hasId(String)}.
 */
public class NoMatchingSamplerFoundException extends PersistenceException {

    public NoMatchingSamplerFoundException(String unusedSamplerId) {
        super("The persistent Sample with the id '%s' doesn't have a corresponding Sampler. " +
                "Please define a Sampler using Sampler.of(...)", unusedSamplerId);
    }

    public NoMatchingSamplerFoundException(Collection<String> unusedSamplerIds) {
        super("The following persistent Samples don't have a corresponding Sampler. " +
                "Please define a Sampler using Sampler.of(...):\n%s", formatMissingSamplerIds(unusedSamplerIds));
    }

    private static String formatMissingSamplerIds(Collection<String> unusedSamplerIds) {
        return unusedSamplerIds.stream()
                .map(id -> "\t" + id)
                .collect(Collectors.joining("\n"));
    }
}
