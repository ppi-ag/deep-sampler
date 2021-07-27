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
import de.ppi.deepsampler.persistence.model.PersistentActualSample;
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
        List<SampleDefinition> loadedSampledDefinitions = new ArrayList<>();
        for (final SourceManager sourceManager: sourceManagerList) {
            final Map<String, SampleDefinition> definedSamples = SampleRepository.getInstance().getSamples().stream()
                    .collect(Collectors.toMap(SampleDefinition::getSampleId, s -> s));
            final PersistentModel persistentModel = sourceManager.load();

            final List<SampleDefinition> filteredMappedSample = toSample(persistentModel, definedSamples);
            loadedSampledDefinitions.addAll(filteredMappedSample);
        }

        List<SampleDefinition> currentSampleDefinitions = SampleRepository.getInstance().getSamples();
        for (int i = currentSampleDefinitions.size() - 1; i >= 0; --i) {
            SampleDefinition definition = currentSampleDefinitions.get(i);
            // we only remove definitions made for the persistence -> definitions without answers
            if (definition.getAnswer() == null) {
                SampleRepository.getInstance().remove(i);
            }
        }

        for (final SampleDefinition sample : loadedSampledDefinitions) {
            SampleRepository.getInstance().add(sample);
        }

        if (SampleRepository.getInstance().isEmpty()) {
            throw new PersistenceException("No Samples from the file could be matched to predefined sampled methods. " +
                    "Did you define sampled methods using Sample.of() in your test?");
        }
    }

    private List<SampleDefinition> toSample(final PersistentModel persistentModel, final Map<String, SampleDefinition> idToSampleMethodMapping) {
        final List<SampleDefinition> samples = new ArrayList<>();
        final Set<SampleDefinition> missedSampleDefinitions = new HashSet<SampleDefinition>();

        for (final Map.Entry<PersistentSampleMethod, PersistentActualSample> persistentSample : persistentModel.getSampleMethodToSampleMap().entrySet()) {
            final PersistentSampleMethod persistentSampleMethod = persistentSample.getKey();
            final PersistentActualSample persistentActualSample = persistentSample.getValue();

            final SampleDefinition matchingSample = idToSampleMethodMapping.get(persistentSampleMethod.getSampleMethodId());

            if (matchingSample != null) {
                for (final PersistentMethodCall call : persistentActualSample.getAllCalls()) {
                    final SampleDefinition sampleDefinition = mapToSample(matchingSample, persistentSampleMethod, call);

                    // We collect all sampleDefinitions in a Set and remove only those sampleDefinitions from this Set, which have a matching Sampler
                    // If the Set is not empty in the end, we know that the persistent SamplerFile (e.g. JSON) contains some unused and most likely changed
                    // Samples. To inform the user about this, we throw an Exception.
                    missedSampleDefinitions.add(sampleDefinition);

                    if (SampleHandling.argumentsMatch(matchingSample, sampleDefinition.getParameterValues().toArray(new Object[0]))) {
                        samples.add(sampleDefinition);
                        missedSampleDefinitions.remove(sampleDefinition);
                    }
                }
            } else {
                throw new NoMatchingSamplerFoundException(persistentSampleMethod.getSampleMethodId());
            }
        }

        if (!missedSampleDefinitions.isEmpty()) {
            throw new ParametersAreNotMatchedException(missedSampleDefinitions);
        }

        return samples;
    }

    private SampleDefinition mapToSample(final SampleDefinition matchingSample, final PersistentSampleMethod persistentSampleMethod,
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
