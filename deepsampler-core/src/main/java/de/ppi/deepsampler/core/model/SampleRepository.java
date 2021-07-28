/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import de.ppi.deepsampler.core.error.NoMatchingParametersFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static de.ppi.deepsampler.core.internal.SampleHandling.argumentsMatch;

public class SampleRepository {

    private List<SampleDefinition> samples = new ArrayList<>();
    private SampleDefinition currentSample;
    private SampleDefinition lastSample;
    private List<ParameterMatcher<?>> currentParameterMatchers = new ArrayList<>();

    private static Scope<SampleRepository> sampleRepositoryScope = new ThreadScope<>();

    /**
     * Singleton Constructor.
     */
    private SampleRepository() {
    }

    public static synchronized SampleRepository getInstance() {
        return sampleRepositoryScope.getOrCreate(SampleRepository::new);
    }

    /**
     * Sets the scope of the {@link SampleRepository} end defines the visibility limits of Samples.
     * The default {@link Scope} is {@link ThreadScope}, so by default Samples are not shared across {@link Thread}s.
     *
     * @param sampleRepositoryScope The {@link Scope} that should be used by the {@link SampleRepository}.
     */
    public static synchronized void setScope(Scope<SampleRepository> sampleRepositoryScope) {
        Objects.requireNonNull(sampleRepositoryScope, "The SampleRepositoryScope must not be null.");

        SampleRepository.sampleRepositoryScope.close();
        SampleRepository.sampleRepositoryScope = sampleRepositoryScope;
    }

    /**
     * Adds the given {@link SampleDefinition} to the {@link SampleRepository#samples}
     * and sets also the {@link SampleRepository#currentSample}.
     *
     * @param sampleDefinition The new SampleDefinition.
     */
    public void add(final SampleDefinition sampleDefinition) {
        setCurrentSample(sampleDefinition);
        samples.add(sampleDefinition);
    }

    /**
     * <p>Removes the SampleDefinition at the given index.</p>
     *
     * <p>Its not possible to remove a given SampleDefinition by
     * equality as a definition do not need to be unique plus the technical equality won't recognize all attributes
     * of the sample.</p>
     *
     * @param index the index at which you want to remove the sample
     */
    public void remove(int index) {
        samples.remove(index);
    }

    /**
     * Checks whether both methods are the same or not
     *
     * @param wantedSampledMethod the sampled method defined by the user
     * @param sampledMethod the actual method the provider came across
     * @return true if both methods are the same
     */
    private boolean methodMatches(SampledMethod wantedSampledMethod, SampledMethod sampledMethod) {
        return sampledMethod.getMethod().equals(wantedSampledMethod.getMethod());
    }

    /**
     * Returns true if the declaring types of wantedSampledMethod and sampledMethod are the same, or  if the declaring
     * type of wantedSampleMethod extends the declaring type of sampledMethod.
     *
     * @param wantedSampledMethod the sampled method defined by the user
     * @param sampledMethod the actual method the provider came across
     * @return true if the type in which the wanted method has been defined matches with the actual method
     */
    private boolean wantedTypeExtendsSampledType(SampledMethod wantedSampledMethod, SampledMethod sampledMethod) {
        return sampledMethod.getTarget().isAssignableFrom(wantedSampledMethod.getTarget());
    }

    public List<SampleDefinition> findAllForMethod(SampledMethod wantedSampledMethod) {
        List<SampleDefinition> sampleDefinitions = new ArrayList<>();
        for (final SampleDefinition sampleDefinition : samples) {
            final SampledMethod sampledMethod = sampleDefinition.getSampledMethod();

            if (wantedTypeExtendsSampledType(wantedSampledMethod, sampledMethod)
                    && methodMatches(wantedSampledMethod, sampledMethod)) {
                sampleDefinitions.add(sampleDefinition);
            }
        }

        return sampleDefinitions;
    }


    public SampleDefinition findValidated(final SampledMethod wantedSampledMethod, final Object... args) {
        return find(true, wantedSampledMethod, args);
    }

    public SampleDefinition findUnvalidated(final SampledMethod wantedSampledMethod, final Object... args) {
        return find(false, wantedSampledMethod, args);
    }

    private SampleDefinition find(final boolean validate, final SampledMethod wantedSampledMethod, final Object... args) {
        boolean matchingMethodFound = false;

        for (final SampleDefinition sampleDefinition : samples) {
            final SampledMethod sampledMethod = sampleDefinition.getSampledMethod();

            if (wantedTypeExtendsSampledType(wantedSampledMethod, sampledMethod)
                    && methodMatches(wantedSampledMethod, sampledMethod)) {

                matchingMethodFound = true;

                if (argumentsMatch(sampleDefinition, args)) {
                    return sampleDefinition;
                }
            }
        }

        if (matchingMethodFound && validate) {
            throw new NoMatchingParametersFoundException(wantedSampledMethod, args);
        }

        return null;
    }

    private void setCurrentSample(final SampleDefinition sampleDefinition) {
        currentSample = sampleDefinition;
    }

    public SampleDefinition getCurrentSampleDefinition() {
        return currentSample;
    }

    public SampleDefinition getLastSampleDefinition() {
        return lastSample;
    }

    public void setLastSampleDefinition(SampleDefinition sampleDefinition) {
        lastSample = sampleDefinition;
    }

    public List<SampleDefinition> getSamples() {
        return Collections.unmodifiableList(samples);
    }

    public void clearCurrentParameterMatchers() {
        currentParameterMatchers = new ArrayList<>();
    }

    public void addCurrentParameterMatchers(ParameterMatcher<?> parameterMatcher) {
        currentParameterMatchers.add(parameterMatcher);
    }

    public void setCurrentParameterMatchers(ParameterMatcher<?> parameterMatcher) {
        currentParameterMatchers.set(currentParameterMatchers.size() - 1, parameterMatcher);
    }

    public ParameterMatcher<?> getLastParameterMatcher() {
        return currentParameterMatchers.get(currentParameterMatchers.size() - 1);
    }

    public List<ParameterMatcher<?>> getCurrentParameterMatchers() {
        return Collections.unmodifiableList(currentParameterMatchers);
    }

    /**
     * Clears the actual set {@link SampleRepository#currentSample} and the {@link SampleRepository#samples}
     */
    public void clear() {
        samples = new ArrayList<>();
        clearCurrentParameterMatchers();
        currentSample = null;
        lastSample = null;
    }

    public boolean isEmpty() {
        return samples.isEmpty();
    }

}
