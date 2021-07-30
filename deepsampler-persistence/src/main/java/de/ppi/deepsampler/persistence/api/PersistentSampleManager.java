/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

import de.ppi.deepsampler.core.api.Matchers;
import de.ppi.deepsampler.core.internal.SampleHandling;
import de.ppi.deepsampler.core.model.*;
import de.ppi.deepsampler.persistence.PersistentSamplerContext;
import de.ppi.deepsampler.persistence.bean.ReflectionTools;
import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.error.ParametersAreNotMatchedException;
import de.ppi.deepsampler.persistence.error.NoMatchingSamplerFoundException;
import de.ppi.deepsampler.persistence.model.PersistentMethodCall;
import de.ppi.deepsampler.persistence.model.PersistentModel;
import de.ppi.deepsampler.persistence.model.PersistentSampleMethod;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@link PersistentSampleManager} is used to handle any provided {@link SourceManager} and to act
 * as a bridge between this manager and the DeepSampler repositories.
 */
public class PersistentSampleManager {
    private final List<SourceManager> sourceManagerList = new ArrayList<>();
    private final PersistentSamplerContext persistentSamplerContext = new PersistentSamplerContext();

    public PersistentSampleManager(final SourceManager sourceManager) {
        addSourceProvider(sourceManager);
    }

    /**
     * Add another {@link SourceManager} to this builder.
     *
     * @param sourceManager the {@link SourceManager}
     * @return this
     */
    public PersistentSampleManager addSource(final SourceManager sourceManager) {
        addSourceProvider(sourceManager);
        return this;
    }

    /**
     * Add a {@link BeanConverterExtension} to the sample manager.
     *
     * @param beanConverterExtension {@link BeanConverterExtension}
     * @return this
     */
    public PersistentSampleManager addBeanExtension(final BeanConverterExtension beanConverterExtension) {
        persistentSamplerContext.addBeanConverterExtension(beanConverterExtension);
        return this;
    }

    /**
     * End of chain method: call {@link SourceManager#save(Map, PersistentSamplerContext)} on all added {@link SourceManager}s.
     */
    public void record() {
        for (final SourceManager sourceManager: sourceManagerList) {
            sourceManager.save(ExecutionRepository.getInstance().getAll(), persistentSamplerContext);
        }
    }

    /**
     * End of chain method: call {@link SourceManager#load()} on all {@link SourceManager}s and write
     * all loaded samples to the DeepSampler repositories.
     */
    public void load() {
        for (final SourceManager sourceManager: sourceManagerList) {
            final PersistentModel persistentModel = sourceManager.load();

            mergeSamplesFromPersistenceIntoSampleRepository(persistentModel);
        }

        if (SampleRepository.getInstance().isEmpty()) {
            throw new PersistenceException("No Samples from the file could be matched to predefined sampled methods. " +
                    "Did you define sampled methods using Sample.of() in your test?");
        }
    }

    /**
     * This method merges the samples from the persistence (e.g. JSON-File) into manually defined samplers and samples. The order of the
     * samplers is defined by the samplers in the test class or the compound. Samples from the file will be inserted in the list of samples
     * at the position where the matching samplers have been defined. This way users can start by defining very specific matchers followed by
     * broader matchers that can serve als alternatives. I.G. someone could first define a matcher that matches only on a parameter of the value
     * "Picard". The second matcher could then by anyString(). The first sample would then be used only if the correct parameter is supplied and in all
     * other cases the second sampler would be used.
     *
     * @param persistentSamples Contains the Samples from the persistence i.e. JSON
     */
    private void mergeSamplesFromPersistenceIntoSampleRepository(final PersistentModel persistentSamples) {
        SampleRepository sampleRepository = SampleRepository.getInstance();

        // This is a Set of all SampleIds from the persistence. Everytime, when a SampleId could be matched to a Sampler from the SampleRepository,
        // the SampleId will be removed from this Set. The remaining SampleIds are unmatched and will be reported by an Exception.
        Set<String> unusedPersistentSampleIds = getPersistentSampleIds(persistentSamples);

        for (int i = 0; i < sampleRepository.size(); i++) {
            SampleDefinition sampler = sampleRepository.get(i);

            if (sampler.getAnswer() != null) {
                // It is not necessary to merge a Sampler, that already has an Answer.
                continue;
            }

            List<SampleDefinition> mergedPersistentSamples = createSampleDefinitionForEachPersistentSample(persistentSamples, sampler);

            sampleRepository.replace(i, mergedPersistentSamples);

            unusedPersistentSampleIds = filterUsedSampleIds(unusedPersistentSampleIds, mergedPersistentSamples);
        }

        if (!unusedPersistentSampleIds.isEmpty()) {
            throw new NoMatchingSamplerFoundException(unusedPersistentSampleIds);
        }

    }

    private Set<String> filterUsedSampleIds(Set<String> unusedPersistentSampleIds, List<SampleDefinition> mergedPersistentCalls) {
        List<String> mergedSampleIds = mergedPersistentCalls.stream().map(SampleDefinition::getSampleId).collect(Collectors.toList());

        return unusedPersistentSampleIds.stream()
                .filter(s -> !mergedSampleIds.contains(s))
                .collect(Collectors.toSet());
    }

    private Set<String> getPersistentSampleIds(PersistentModel persistentSamples) {
        return persistentSamples.getSampleMethodToSampleMap().keySet().stream()
                .map(PersistentSampleMethod::getSampleMethodId)
                .collect(Collectors.toSet());
    }

    private List<SampleDefinition> createSampleDefinitionForEachPersistentSample(PersistentModel persistentSamples, SampleDefinition sampler) {
        List<SampleDefinition> usedPersistentCalls = new ArrayList<>();
        Set<SampleDefinition> unusedPersistentCalls = new HashSet<>();

        for(PersistentSampleMethod persistentSampleMethod : persistentSamples.getSampleMethodToSampleMap().keySet()) {

            if (persistentSampleMethod.getSampleMethodId().equals(sampler.getSampleId())) {
                List<PersistentMethodCall> calls = persistentSamples.getSampleMethodToSampleMap().get(persistentSampleMethod).getAllCalls();

                for (PersistentMethodCall call : calls) {
                    SampleDefinition combinedSampleDefinition = combinePersistentSampleAndDefinedSampler(sampler, persistentSampleMethod, call);
                    // We use the parameter values from combinedSampleDefinition because combinePersistentSampleAndDefinedSampler() unwraps the
                    // parameters from persistence containers.
                    Object[] actualParameterValues = combinedSampleDefinition.getParameterValues().toArray();

                    unusedPersistentCalls.add(combinedSampleDefinition);

                    if (SampleHandling.argumentsMatch(sampler, actualParameterValues)) {
                        usedPersistentCalls.add(combinedSampleDefinition);
                        unusedPersistentCalls.remove(combinedSampleDefinition);
                    }
                }

            }
        }

        if (!unusedPersistentCalls.isEmpty()) {
            throw new ParametersAreNotMatchedException(unusedPersistentCalls);
        }

        return usedPersistentCalls;
    }


    private SampleDefinition combinePersistentSampleAndDefinedSampler(final SampleDefinition matchingSample, final PersistentSampleMethod persistentSampleMethod,
                                                                      final PersistentMethodCall call) {
        final List<Object> parameterEnvelopes = call.getPersistentParameter().getParameter();
        final Object returnValueEnvelope = call.getPersistentReturnValue();
        final SampledMethod sampledMethod = matchingSample.getSampledMethod();
        final Type[] parameterTypes = sampledMethod.getMethod().getGenericParameterTypes();
        final Type genericReturnType = sampledMethod.getMethod().getGenericReturnType();
        final ParameterizedType parameterizedReturnType = genericReturnType instanceof ParameterizedType ? (ParameterizedType) genericReturnType : null;
        final Class<?> returnClass = sampledMethod.getMethod().getReturnType();
        final String joinPointId = persistentSampleMethod.getSampleMethodId();

        final List<Object> parameterValues = unwrapValue(joinPointId, parameterTypes, parameterEnvelopes);
        final List<ParameterMatcher<?>> parameterMatchers = toMatcher(parameterValues, matchingSample.getParameterMatchers());

        final SampleDefinition sample = new SampleDefinition(sampledMethod);
        sample.setSampleId(joinPointId);
        sample.setParameterMatchers(parameterMatchers);
        sample.setParameterValues(parameterValues);

        final Object returnValue = unwrapValue(returnClass, parameterizedReturnType, returnValueEnvelope);
        sample.setAnswer(invocation -> returnValue);

        return sample;
    }

    private List<Object> unwrapValue(final String id, final Type[] parameterTypes, final List<Object> parameterPersistentBeans) {
        final List<Object> params = new ArrayList<>();

        if (parameterTypes.length != parameterPersistentBeans.size()) {
            throw new PersistenceException("The number of parameters from the method of %s does " +
                    "not match the number of persistent parameters (%s:%s)!", id, parameterTypes, parameterPersistentBeans);
        }
        for (int i = 0; i < parameterPersistentBeans.size(); ++i) {
            final ParameterizedType parameterType = parameterTypes[i] instanceof  ParameterizedType ? (ParameterizedType) parameterTypes[i] : null;
            final Class<?> parameterClass = ReflectionTools.getClass(parameterTypes[i]);
            final Object persistentBean = parameterPersistentBeans.get(i);

            params.add(unwrapValue(parameterClass, parameterType, persistentBean));
        }
        return params;
    }



    private Object unwrapValue(final Class<?> targetClass, final ParameterizedType type, final Object persistentBean) {
        return persistentSamplerContext.getPersistentBeanConverter().revert(persistentBean, targetClass, type);
    }

    @SuppressWarnings("unchecked")
    private List<ParameterMatcher<?>> toMatcher(final List<Object> params, List<ParameterMatcher<?>> parameterMatchers) {
        List<ParameterMatcher<?>> resultingParameterMatcher = new ArrayList<>();
        for (int i = 0; i < params.size(); ++i) {
            Object param = params.get(i);
            ParameterMatcher<?> parameterMatcher  = parameterMatchers.get(i);

            if (parameterMatcher instanceof ComboMatcher) {
                resultingParameterMatcher.add(s -> ((ComboMatcher<Object>) parameterMatcher).getPersistentMatcher().matches(s, param));
            } else {
                resultingParameterMatcher.add(new Matchers.EqualsMatcher<>(param));
            }
        }
        return resultingParameterMatcher;
    }

    private void addSourceProvider(final SourceManager sourceManager) {
        this.sourceManagerList.add(sourceManager);
    }

}
