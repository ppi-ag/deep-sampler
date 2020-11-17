/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.model;

import de.ppi.deepsampler.core.error.DuplicateSampleDefinitionException;

import java.util.ArrayList;
import java.util.List;

public class SampleRepository {

    private final ThreadLocal<List<SampleDefinition>> samples = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<SampleDefinition> currentSample = new ThreadLocal<>();
    private final ThreadLocal<List<ParameterMatcher<?>>> currentParameterMatchers = ThreadLocal.withInitial(ArrayList::new);

    private static SampleRepository myInstance;

    /**
     * Singleton Constructor.
     */
    private SampleRepository() {}

    public static synchronized SampleRepository getInstance() {
        if (myInstance == null) {
            myInstance = new SampleRepository();
        }

        return myInstance;
    }

    /**
     * Adds the given {@link SampleDefinition} to the {@link SampleRepository#samples}
     * and sets also the {@link SampleRepository#currentSample}.
     *
     * @param sampleDefinition The new SampleDefinition.
     */
    public void add(final SampleDefinition sampleDefinition) {
        final SampleDefinition currentSampleDefinition = getCurrentSampleDefinition();
        if(sampleDefinition.equals(currentSampleDefinition)
                || samples.get().contains(sampleDefinition)) {
            throw new DuplicateSampleDefinitionException(sampleDefinition);
        }
        setCurrentSample(sampleDefinition);
        samples.get().add(sampleDefinition);
    }

    public List<SampleDefinition> findAllForMethod(SampledMethod wantedSampledMethod) {
        List<SampleDefinition> sampleDefinitions = new ArrayList<>();
        for (final SampleDefinition sampleDefinition : samples.get()) {
            final SampledMethod sampledMethod = sampleDefinition.getSampledMethod();
            final boolean classMatches = sampledMethod.getTarget().isAssignableFrom(wantedSampledMethod.getTarget());
            final boolean methodMatches = sampledMethod.getMethod().equals(wantedSampledMethod.getMethod());

            if (classMatches && methodMatches) {
                sampleDefinitions.add(sampleDefinition);
            }
        }

        return sampleDefinitions;
    }

    public SampleDefinition find(final SampledMethod wantedSampledMethod, final Object... args) {
        for (final SampleDefinition sampleDefinition : samples.get()) {
            final SampledMethod sampledMethod = sampleDefinition.getSampledMethod();
            final boolean classMatches = sampledMethod.getTarget().isAssignableFrom(wantedSampledMethod.getTarget());
            final boolean methodMatches = sampledMethod.getMethod().equals(wantedSampledMethod.getMethod());
            final boolean argumentsMatches = argumentsMatch(sampleDefinition, args);

            if (classMatches && methodMatches && argumentsMatches) {
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
        currentSample.set(sampleDefinition);
    }

    public SampleDefinition getCurrentSampleDefinition() {
        return currentSample.get();
    }

    public List<SampleDefinition> getSamples() {
        return samples.get();
    }

    public void clearCurrentParameterMatchers() {
        currentParameterMatchers.set(new ArrayList<>());
    }

    public void addCurrentParameterMatchers(ParameterMatcher<?> parameterMatcher) {
        currentParameterMatchers.get().add(parameterMatcher);
    }

    public List<ParameterMatcher<?>> getCurrentParameterMatchers() {
        return currentParameterMatchers.get();
    }

    /**
     * Clears the actual set {@link SampleRepository#currentSample} and the {@link SampleRepository#samples}
     */
    public void clear() {
        samples.get().clear();
        samples.remove();
        currentParameterMatchers.remove();
        currentSample.remove();
    }

    public boolean isEmpty() {
        return samples.get() == null || samples.get().isEmpty();
    }

}
