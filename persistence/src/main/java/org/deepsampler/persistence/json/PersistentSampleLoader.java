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

    public PersistentSampleLoader(SourceManager sourceManager) {
        addSourceProvider(sourceManager);
    }

    public PersistentSampleLoader source(SourceManager sourceManager) {
        addSourceProvider(sourceManager);
        return this;
    }

    public void record() {
        for (SourceManager sourceManager: sourceManagerList) {
            sourceManager.record(ExecutionRepository.getInstance().getAll());
        }
    }

    public void load() {
        for (SourceManager sourceManager: sourceManagerList) {
            Map<String, SampledMethod> definedSamples = SampleRepository.getInstance().getSamples().stream()
                    .collect(Collectors.toMap(SampleDefinition::getSampleId, SampleDefinition::getSampledMethod));
            PersistentModel persistentModel = sourceManager.load();

            List<SampleDefinition> filteredMappedSample = toSample(persistentModel, definedSamples);

            SampleRepository.getInstance().clear();

            for (SampleDefinition sample : filteredMappedSample) {
                SampleRepository.getInstance().add(sample);
            }
        }
    }

    private List<SampleDefinition> toSample(PersistentModel model, Map<String, SampledMethod> idToJp) {
        List<SampleDefinition> samples = new ArrayList<>();

        for (Map.Entry<PersistentSampleMethod, PersistentActualSample> joinPointBehaviorEntry : model.getSampleMethodToSampleMap().entrySet()) {
            PersistentSampleMethod persistentSampleMethod = joinPointBehaviorEntry.getKey();
            PersistentActualSample persistentActualSample = joinPointBehaviorEntry.getValue();
            SampledMethod matchingSample = idToJp.get(persistentSampleMethod.getSampleMethodId());

            // When there is no matching JointPoint, the persistentJoinPointEntity will be discarded
            if (matchingSample != null) {
                for (PersistentMethodCall call : persistentActualSample.getAllCalls()) {
                    SampleDefinition behavior = mapToSample(matchingSample, persistentSampleMethod, call);
                    samples.add(behavior);
                }
            }
        }
        return samples;
    }

    private SampleDefinition mapToSample(SampledMethod matchingJointPoint, PersistentSampleMethod persistentSampleMethod,
                                         PersistentMethodCall call) {
        List<Object> parameter = call.getPersistentParameter().getParameter();
        Object returnValue = call.getPersistentReturnValue().getReturnValue();
        Class<?>[] parameters = matchingJointPoint.getMethod().getParameterTypes();
        Class<?> returnType = matchingJointPoint.getMethod().getReturnType();
        String joinPointId = persistentSampleMethod.getSampleMethodId();

        SampleDefinition sample = new SampleDefinition(matchingJointPoint);
        sample.setSampleId(joinPointId);
        sample.setParameter(toMatcher(toRealValue(joinPointId, parameters, parameter)));
        sample.setReturnValueSupplier(() -> toRealValue(returnType, returnValue));
        return sample;
    }

    private List<Object> toRealValue(String id, Class<?>[] parameters, List<Object> parameterPersistentBeans) {
        List<Object> params = new ArrayList<>();

        if (parameters.length != parameterPersistentBeans.size()) {
            throw new PersistenceException("The number of parameters from the method of %s does " +
                    "not match the number of persistent parameters (%s:%s)!", id, parameters, parameterPersistentBeans);
        }
        for (int i = 0; i < parameterPersistentBeans.size(); ++i) {
            Class<?> parameter = parameters[i];
            Object persistentBean = parameterPersistentBeans.get(i);
            params.add(toRealValue(parameter, persistentBean));
        }
        return params;
    }

    private Object toRealValue(Class<?> type, Object persistentBean) {
        return PersistentBeanFactory.ofBeanIfNecessary(persistentBean, type);
    }

    private List<ParameterMatcher> toMatcher(List<Object> params) {
        return params.stream()
                .map(Matchers::equalTo)
                .collect(Collectors.toList());
    }

    private void addSourceProvider(SourceManager sourceManager) {
        this.sourceManagerList.add(sourceManager);
    }

}
