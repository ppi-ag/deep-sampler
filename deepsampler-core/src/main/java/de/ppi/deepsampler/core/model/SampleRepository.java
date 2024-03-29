/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
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

    private boolean markNextVoidSamplerForPersistence = false;

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
     * Replaces the {@link SampleDefinition} at index i with the {@link SampleDefinition}s from mergedPersistentSamples. If
     * the {@link List} is longer then 1 all {@link SampleDefinition}s after i are moved to the right.
     * @param i The index of the {@link SampleDefinition} that should be replaced.
     * @param mergedPersistentSamples The {@link SampleDefinition}s that are inserted at i.
     */
    public void replace(int i, List<SampleDefinition> mergedPersistentSamples) {
        samples.addAll(i, mergedPersistentSamples);
        samples.remove(i + mergedPersistentSamples.size());
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
     * Returns the number of samples in this {@link SampleRepository}
     * @return Returns the number of samples in this {@link SampleRepository}
     */
    public int size() {
        return samples.size();
    }

    /**
     * Returns the {@link SampleDefinition} and index.
     * @param index The index of the requested {@link SampleDefinition}.
     * @return The {@link SampleDefinition} and index.
     */
    public SampleDefinition get(int index) {
        return samples.get(index);
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


    /**
     * Searches for a {@link SampleDefinition} that matches to wantedSampleMethod and the parameters args
     * The search is validated, which means, that an {@link NoMatchingParametersFoundException} is thrown if no {@link SampleDefinition}
     * could be found.
     *
     * @param wantedSampledMethod Describes the method for which a {@link SampleDefinition} is searched
     * @param args the actual parameter values that should match to the matchers of a SampleDefinition.
     * @return A matching {@link SampleDefinition} if one was found. Otherwise a {@link NoMatchingParametersFoundException} is thrown.
     * @throws NoMatchingParametersFoundException if no matching {@link SampleDefinition} was found.
     */
    public SampleDefinition findValidated(final SampledMethod wantedSampledMethod, final Object... args) {
        return find(true, wantedSampledMethod, args);
    }

    /**
     * Searches for a {@link SampleDefinition} that matches to wantedSampleMethod and the parameters args
     * The search is not validated, which means, that null is returned if no {@link SampleDefinition}
     * could be found.
     *
     * @param wantedSampledMethod Describes the method for which a {@link SampleDefinition} is searched
     * @param args the actual parameter values that should match to the matchers of a SampleDefinition.
     * @return A matching {@link SampleDefinition} if one was found. Otherwise null.
     */
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
     * If the next newly created SampleDefinition should be marked for persistence, this getter would return true.
     * This is used for void methods where the SampleDefinition is not reachable from PersistentSampler.
     *
     * @return true if the next SampleDefinition should be marked for persistence.
     */
    public boolean getMarkNextVoidSamplerForPersistence() {
        return markNextVoidSamplerForPersistence;
    }

    /**
     * If the next newly created SampleDefinition should be marked for persistence, markNextVoidSamplerForPersistence can be set to true.
     * This is used for void methods where the SampleDefinition is not reachable from PersistentSampler.
     *
     * @param markNextVoidSamplerForPersistence true if the next SampleDefinition should be marked for persistence.
     */
    public void setMarkNextVoidSamplerForPersistence(boolean markNextVoidSamplerForPersistence) {
        this.markNextVoidSamplerForPersistence = markNextVoidSamplerForPersistence;
    }

    public void clearMarkNextVoidSamplerForPersistence() {
        this.markNextVoidSamplerForPersistence = false;
    }

    /**
     * Clears the actual set {@link SampleRepository#currentSample} and the {@link SampleRepository#samples}
     */
    public void clear() {
        samples = new ArrayList<>();
        clearCurrentParameterMatchers();
        clearMarkNextVoidSamplerForPersistence();
        currentSample = null;
        lastSample = null;
    }

    public boolean isEmpty() {
        return samples.isEmpty();
    }


}
