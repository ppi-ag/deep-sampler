package org.deepmock.persistence;

import org.deepmock.core.api.Matchers;
import org.deepmock.core.model.*;
import org.deepmock.persistence.bean.Bean;
import org.deepmock.persistence.bean.BeanFactory;
import org.deepmock.persistence.error.PersistenceException;
import org.deepmock.persistence.model.PersistentActualBehavior;
import org.deepmock.persistence.model.PersistentJoinPoint;
import org.deepmock.persistence.model.PersistentMethodCall;
import org.deepmock.persistence.model.PersistentModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersistentPersonalityLoader {
    private final List<SourceManager> sourceManagerList = new ArrayList<>();

    public PersistentPersonalityLoader(SourceManager sourceManager) {
        addSourceProvider(sourceManager);
    }

    public PersistentPersonalityLoader source(SourceManager sourceManager) {
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
            Map<String, JoinPoint> definedBehaviors = BehaviorRepository.getInstance().getCurrentExecutionBehaviors().stream()
                    .collect(Collectors.toMap(Behavior::getBehaviorId, Behavior::getJoinPoint));
            PersistentModel persistentModel = sourceManager.load();

            List<Behavior> filteredMappedBehaviors = toBehaviors(persistentModel, definedBehaviors);

            for (Behavior behavior : filteredMappedBehaviors) {
                BehaviorRepository.getInstance().add(behavior);
            }
        }
    }

    private List<Behavior> toBehaviors(PersistentModel model, Map<String, JoinPoint> idToJp) {
        List<Behavior> behaviors = new ArrayList<>();

        for (Map.Entry<PersistentJoinPoint, PersistentActualBehavior> joinPointBehaviorEntry : model.getJoinPointBehaviorMap().entrySet()) {
            PersistentJoinPoint persistentJoinPoint = joinPointBehaviorEntry.getKey();
            PersistentActualBehavior persistentActualBehavior = joinPointBehaviorEntry.getValue();
            JoinPoint matchingJointPoint = idToJp.get(persistentJoinPoint.getJoinPointId());

            // When there is no matching JointPoint, the persistentJoinPointEntity will be discarded
            if (matchingJointPoint != null) {
                for (PersistentMethodCall call : persistentActualBehavior.getAllCalls()) {
                    Behavior behavior = mapToBehavior(matchingJointPoint, persistentJoinPoint, call);
                    behaviors.add(behavior);
                }
            }
        }
        return behaviors;
    }

    private Behavior mapToBehavior(JoinPoint matchingJointPoint, PersistentJoinPoint persistentJoinPoint,
                                   PersistentMethodCall call) {
        List<Bean> parameter = call.getPersistentParameter().getParameter();
        Bean returnValue = call.getPersistentReturnValue().getReturnValue();
        Class<?>[] parameters = matchingJointPoint.getMethod().getParameterTypes();
        Class<?> returnType = matchingJointPoint.getMethod().getReturnType();
        String joinPointId = persistentJoinPoint.getJoinPointId();

        Behavior behavior = new Behavior(matchingJointPoint);
        behavior.setBehaviorId(joinPointId);
        behavior.setParameter(toMatcher(toRealValue(joinPointId, parameters, parameter)));
        behavior.setReturnValueSupplier(() -> toRealValue(returnType, returnValue));
        return behavior;
    }

    private List<Object> toRealValue(String id, Class<?>[] parameters, List<Bean> parameterBeans) {
        List<Object> params = new ArrayList<>();

        if (parameters.length != parameterBeans.size()) {
            throw new PersistenceException("The number of parameters from the method of %s does " +
                    "not match the number of persistent parameters (%s:%s)!", id, parameters, parameterBeans);
        }
        for (int i = 0; i < parameterBeans.size(); ++i) {
            Class<?> parameter = parameters[i];
            Bean bean = parameterBeans.get(i);
            params.add(toRealValue(parameter, bean));
        }
        return params;
    }

    private Object toRealValue(Class<?> type, Bean bean) {
        return BeanFactory.ofBean(bean, type);
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
