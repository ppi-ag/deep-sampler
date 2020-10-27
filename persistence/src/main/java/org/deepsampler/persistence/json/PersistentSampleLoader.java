package org.deepsampler.persistence.json;

import org.deepsampler.core.api.Matchers;
import org.deepsampler.core.model.*;
import org.deepsampler.persistence.json.bean.PersistentBeanFactory;
import org.deepsampler.persistence.json.error.PersistenceException;
import org.deepsampler.persistence.json.model.PersistentActualSample;
import org.deepsampler.persistence.json.model.PersistentMethodCall;
import org.deepsampler.persistence.json.model.PersistentModel;
import org.deepsampler.persistence.json.model.PersistentSampleMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersistentSampleLoader {
    private final List<SourceManager> sourceManagerList = new ArrayList<>();

    public PersistentSampleLoader(final SourceManager sourceManager) {
        addSourceProvider(sourceManager);
    }

    public PersistentSampleLoader source(final SourceManager sourceManager) {
        addSourceProvider(sourceManager);
        return this;
    }

    public void record() {
        for (final SourceManager sourceManager: sourceManagerList) {
            sourceManager.record(ExecutionRepository.getInstance().getAll());
        }
    }

    public void load() {
        for (final SourceManager sourceManager: sourceManagerList) {
            final Map<String, SampledMethod> definedSamples = SampleRepository.getInstance().getSamples().stream()
                    .collect(Collectors.toMap(SampleDefinition::getSampleId, SampleDefinition::getSampledMethod));
            final PersistentModel persistentModel = sourceManager.load();

            final List<SampleDefinition> filteredMappedSample = toSample(persistentModel, definedSamples);

            SampleRepository.getInstance().clear();

            for (final SampleDefinition sample : filteredMappedSample) {
                SampleRepository.getInstance().add(sample);
            }
        }
    }

    private List<SampleDefinition> toSample(final PersistentModel model, final Map<String, SampledMethod> idToJp) {
        final List<SampleDefinition> samples = new ArrayList<>();

        for (final Map.Entry<PersistentSampleMethod, PersistentActualSample> joinPointBehaviorEntry : model.getSampleMethodToSampleMap().entrySet()) {
            final PersistentSampleMethod persistentSampleMethod = joinPointBehaviorEntry.getKey();
            final PersistentActualSample persistentActualSample = joinPointBehaviorEntry.getValue();
            final SampledMethod matchingSample = idToJp.get(persistentSampleMethod.getSampleMethodId());

            // When there is no matching JointPoint, the persistentJoinPointEntity will be discarded
            if (matchingSample != null) {
                for (final PersistentMethodCall call : persistentActualSample.getAllCalls()) {
                    final SampleDefinition behavior = mapToSample(matchingSample, persistentSampleMethod, call);
                    samples.add(behavior);
                }
            }
        }
        return samples;
    }

    private SampleDefinition mapToSample(final SampledMethod matchingJointPoint, final PersistentSampleMethod persistentSampleMethod,
                                         final PersistentMethodCall call) {
        final List<Object> parameter = call.getPersistentParameter().getParameter();
        final Object returnValue = call.getPersistentReturnValue().getReturnValue();
        final Class<?>[] parameters = matchingJointPoint.getMethod().getParameterTypes();
        final Class<?> returnType = matchingJointPoint.getMethod().getReturnType();
        final String joinPointId = persistentSampleMethod.getSampleMethodId();

        final SampleDefinition sample = new SampleDefinition(matchingJointPoint);
        sample.setSampleId(joinPointId);
        sample.setParameter(toMatcher(toRealValue(joinPointId, parameters, parameter)));
        sample.setReturnValueSupplier(() -> toRealValue(returnType, returnValue));
        return sample;
    }

    private List<Object> toRealValue(final String id, final Class<?>[] parameters, final List<Object> parameterPersistentBeans) {
        final List<Object> params = new ArrayList<>();

        if (parameters.length != parameterPersistentBeans.size()) {
            throw new PersistenceException("The number of parameters from the method of %s does " +
                    "not match the number of persistent parameters (%s:%s)!", id, parameters, parameterPersistentBeans);
        }
        for (int i = 0; i < parameterPersistentBeans.size(); ++i) {
            final Class<?> parameter = parameters[i];
            final Object persistentBean = parameterPersistentBeans.get(i);
            params.add(toRealValue(parameter, persistentBean));
        }
        return params;
    }

    private Object toRealValue(final Class<?> type, final Object persistentBean) {
        return PersistentBeanFactory.ofBeanIfNecessary(persistentBean, type);
    }

    private List<ParameterMatcher> toMatcher(final List<Object> params) {
        return params.stream()
                .map(Matchers.EqualsMatcher::new)
                .collect(Collectors.toList());
    }

    private void addSourceProvider(final SourceManager sourceManager) {
        this.sourceManagerList.add(sourceManager);
    }

}
