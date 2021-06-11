/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.api;

import de.ppi.deepsampler.core.api.Matchers;
import de.ppi.deepsampler.core.internal.SampleHandling;
import de.ppi.deepsampler.core.model.*;
import de.ppi.deepsampler.persistence.PersistentSamplerContext;
import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.model.PersistentActualSample;
import de.ppi.deepsampler.persistence.model.PersistentMethodCall;
import de.ppi.deepsampler.persistence.model.PersistentModel;
import de.ppi.deepsampler.persistence.model.PersistentSampleMethod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private List<SampleDefinition> toSample(final PersistentModel model, final Map<String, SampleDefinition> idToSampleMethodMapping) {
        final List<SampleDefinition> samples = new ArrayList<>();

        for (final Map.Entry<PersistentSampleMethod, PersistentActualSample> joinPointBehaviorEntry : model.getSampleMethodToSampleMap().entrySet()) {
            final PersistentSampleMethod persistentSampleMethod = joinPointBehaviorEntry.getKey();
            final PersistentActualSample persistentActualSample = joinPointBehaviorEntry.getValue();
            final SampleDefinition matchingSample = idToSampleMethodMapping.get(persistentSampleMethod.getSampleMethodId());

            // When there is no matching JointPoint, the persistentJoinPointEntity will be discarded
            if (matchingSample != null) {
                for (final PersistentMethodCall call : persistentActualSample.getAllCalls()) {
                    final SampleDefinition behavior = mapToSample(matchingSample, persistentSampleMethod, call);
                    if (SampleHandling.argumentsMatch(matchingSample, behavior.getParameterValues().toArray(new Object[0]))) {
                        samples.add(behavior);
                    }
                }
            }
        }
        return samples;
    }

    private SampleDefinition mapToSample(final SampleDefinition matchingSample, final PersistentSampleMethod persistentSampleMethod,
                                         final PersistentMethodCall call) {
        final List<Object> parameterEnvelopes = call.getPersistentParameter().getParameter();
        final Object returnValueEnvelope = call.getPersistentReturnValue();
        final SampledMethod sampledMethod = matchingSample.getSampledMethod();
        final Class<?>[] parameterTypes = sampledMethod.getMethod().getParameterTypes();
        final Type returnType = sampledMethod.getMethod().getGenericReturnType();
        final String joinPointId = persistentSampleMethod.getSampleMethodId();

        final List<Object> parameterValues = unwrapValue(joinPointId, parameterTypes, parameterEnvelopes);
        final List<ParameterMatcher<?>> parameterMatchers = toMatcher(parameterValues, matchingSample.getParameterMatchers());

        final SampleDefinition sample = new SampleDefinition(sampledMethod);
        sample.setSampleId(joinPointId);
        sample.setParameterMatchers(parameterMatchers);
        sample.setParameterValues(parameterValues);

        final Object returnValue = unwrapValue(returnType, returnValueEnvelope);
        sample.setAnswer(invocation -> returnValue);

        return sample;
    }

    private List<Object> unwrapValue(final String id, final Class<?>[] parameterTypes, final List<Object> parameterPersistentBeans) {
        final List<Object> params = new ArrayList<>();

        if (parameterTypes.length != parameterPersistentBeans.size()) {
            throw new PersistenceException("The number of parameters from the method of %s does " +
                    "not match the number of persistent parameters (%s:%s)!", id, parameterTypes, parameterPersistentBeans);
        }
        for (int i = 0; i < parameterPersistentBeans.size(); ++i) {
            final Class<?> parameterType = parameterTypes[i];
            final Object persistentBean = parameterPersistentBeans.get(i);
            params.add(unwrapValue(parameterType, persistentBean));
        }
        return params;
    }

    private Object unwrapValue(final Type type, final Object persistentBean) {
        return persistentSamplerContext.getPersistentBeanConverter().revert(persistentBean, type);
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
