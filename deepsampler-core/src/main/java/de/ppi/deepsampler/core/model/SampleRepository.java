/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SampleRepository {

    private List<SampleDefinition> samples = new ArrayList<>();
    private SampleDefinition currentSample;
    private SampleDefinition lastSample;
    private List<ParameterMatcher<?>> currentParameterMatchers = new ArrayList<>();

    private static Scope sampleRepositoryScope = new ThreadScope();

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
    public static synchronized void setScope(Scope sampleRepositoryScope) {
        Objects.requireNonNull(sampleRepositoryScope, "The SampleRepositoryScope must not be null.");

        SampleRepository.sampleRepositoryScope.cleanUp();
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
     * Checks whether both methods are the same or not
     *
     * @param wantedSampledMethod
     * @param sampledMethod
     * @return true if both methods are the same
     */
    private boolean methodMatches(SampledMethod wantedSampledMethod, SampledMethod sampledMethod) {
        return sampledMethod.getMethod().equals(wantedSampledMethod.getMethod());
    }

    /**
     * Returns true if the declaring types of wantedSampledMethod and sampledMethod are the same, or  if the declaring
     * type of wantedSampleMethod extends the declaring type of sampledMethod.
     *
     * @param wantedSampledMethod
     * @param sampledMethod
     * @return
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


    public SampleDefinition find(final SampledMethod wantedSampledMethod, final Object... args) {
        for (final SampleDefinition sampleDefinition : samples) {
            final SampledMethod sampledMethod = sampleDefinition.getSampledMethod();

            if (wantedTypeExtendsSampledType(wantedSampledMethod, sampledMethod)
                    && methodMatches(wantedSampledMethod, sampledMethod)
                    && argumentsMatch(sampleDefinition, args)) {

                return sampleDefinition;
            }
        }

        return null;
    }


    @SuppressWarnings("unchecked")
    private boolean argumentsMatch(final SampleDefinition sampleDefinition, final Object[] arguments) {
        final List<ParameterMatcher<?>> parameterMatchers = sampleDefinition.getParameterMatchers();

        if (parameterMatchers.size() != arguments.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            final ParameterMatcher<Object> parameterMatcher = (ParameterMatcher<Object>) parameterMatchers.get(i);
            if (!parameterMatcher.matches(arguments[i])) {
                return false;
            }
        }

        return true;
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
        return samples;
    }

    public void clearCurrentParameterMatchers() {
        currentParameterMatchers = new ArrayList<>();
    }

    public void addCurrentParameterMatchers(ParameterMatcher<?> parameterMatcher) {
        currentParameterMatchers.add(parameterMatcher);
    }

    public List<ParameterMatcher<?>> getCurrentParameterMatchers() {
        return currentParameterMatchers;
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
