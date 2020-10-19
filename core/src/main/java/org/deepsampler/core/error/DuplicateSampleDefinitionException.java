package org.deepsampler.core.error;

import org.deepsampler.core.model.SampleDefinition;

/**
 * Exception type that is thrown while trying to add a duplicated {@link SampleDefinition}
 * to the {@link org.deepsampler.core.model.SampleRepository}
 *
 * @author Hendrik Surma
 */
public class DuplicateSampleDefinitionException extends BaseException {

    /**
     * Message to show when this exception occurs.
     */
    public static final String MESSAGE = "The SampleDefinition with ID '%s' for Methodcall '%s' " +
                    "has already been sampled. " +
                    "Keep in mind to sample methodcalls only once!";

    /**
     * Constructor.
     *
     * @param sampleDefinition the already existing instance of {@link SampleDefinition}
     */
    public DuplicateSampleDefinitionException(final SampleDefinition sampleDefinition) {
        super(MESSAGE, sampleDefinition.getSampleId(), sampleDefinition.getSampledMethod().getMethod().getName());
    }
}
