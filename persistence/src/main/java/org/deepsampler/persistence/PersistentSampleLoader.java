package org.deepsampler.persistence;

import org.deepsampler.persistence.bean.PersistentBean;
import org.deepsampler.persistence.bean.PersistentBeanFactory;
import org.deepsampler.persistence.error.PersistenceException;
import org.deepsampler.persistence.model.PersistentActualSample;
import org.deepsampler.persistence.model.PersistentSampleMethod;
import org.deepsampler.persistence.model.PersistentMethodCall;
import org.deepsampler.persistence.model.PersistentModel;
import org.deepsampler.core.api.Matchers;
import org.deepsampler.core.model.*;

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
            Map<String, SampledMethod> definedBehaviors = SampleRepository.getInstance().getSamples().stream()
                    .collect(Collectors.toMap(SampleDefinition::getSampleId, SampleDefinition::getSampledMethod));
            PersistentModel persistentModel = sourceManager.load();

            List<SampleDefinition> filteredMappedBehaviors = toBehaviors(persistentModel, definedBehaviors);

            for (SampleDefinition behavior : filteredMappedBehaviors) {
                SampleRepository.getInstance().add(behavior);
            }
        }
    }

    private List<SampleDefinition> toBehaviors(PersistentModel model, Map<String, SampledMethod> idToJp) {
        List<SampleDefinition> behaviors = new ArrayList<>();

        for (Map.Entry<PersistentSampleMethod, PersistentActualSample> joinPointBehaviorEntry : model.getJoinPointBehaviorMap().entrySet()) {
            PersistentSampleMethod persistentSampleMethod = joinPointBehaviorEntry.getKey();
            PersistentActualSample persistentActualSample = joinPointBehaviorEntry.getValue();
            SampledMethod matchingJointPoint = idToJp.get(persistentSampleMethod.getJoinPointId());

            // When there is no matching JointPoint, the persistentJoinPointEntity will be discarded
            if (matchingJointPoint != null) {
                for (PersistentMethodCall call : persistentActualSample.getAllCalls()) {
                    SampleDefinition behavior = mapToBehavior(matchingJointPoint, persistentSampleMethod, call);
                    behaviors.add(behavior);
                }
            }
        }
        return behaviors;
    }

    private SampleDefinition mapToBehavior(SampledMethod matchingJointPoint, PersistentSampleMethod persistentSampleMethod,
                                   PersistentMethodCall call) {
        List<PersistentBean> parameter = call.getPersistentParameter().getParameter();
        PersistentBean returnValue = call.getPersistentReturnValue().getReturnValue();
        Class<?>[] parameters = matchingJointPoint.getMethod().getParameterTypes();
        Class<?> returnType = matchingJointPoint.getMethod().getReturnType();
        String joinPointId = persistentSampleMethod.getJoinPointId();

        SampleDefinition behavior = new SampleDefinition(matchingJointPoint);
        behavior.setBehaviorId(joinPointId);
        behavior.setParameter(toMatcher(toRealValue(joinPointId, parameters, parameter)));
        behavior.setReturnValueSupplier(() -> toRealValue(returnType, returnValue));
        return behavior;
    }

    private List<Object> toRealValue(String id, Class<?>[] parameters, List<PersistentBean> parameterPersistentBeans) {
        List<Object> params = new ArrayList<>();

        if (parameters.length != parameterPersistentBeans.size()) {
            throw new PersistenceException("The number of parameters from the method of %s does " +
                    "not match the number of persistent parameters (%s:%s)!", id, parameters, parameterPersistentBeans);
        }
        for (int i = 0; i < parameterPersistentBeans.size(); ++i) {
            Class<?> parameter = parameters[i];
            PersistentBean persistentBean = parameterPersistentBeans.get(i);
            params.add(toRealValue(parameter, persistentBean));
        }
        return params;
    }

    private Object toRealValue(Class<?> type, PersistentBean persistentBean) {
        return PersistentBeanFactory.ofBean(persistentBean, type);
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
