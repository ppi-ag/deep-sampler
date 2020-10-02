package org.deepmock.persistence.json;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorExecutionInformation;
import org.deepmock.core.model.ExecutionInformation;
import org.deepmock.core.model.MethodCall;
import org.deepmock.persistence.bean.Bean;
import org.deepmock.persistence.bean.BeanFactory;
import org.deepmock.persistence.json.error.JsonPersistenceException;
import org.deepmock.persistence.json.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonRecorder extends JsonOperator {
    private final Path path;

    public JsonRecorder(Path path) {
        this.path = path;
    }

    public void record(Map<Class<?>, ExecutionInformation> executionInformationMap) {
        try {
            // CREATE PARENT DIR IF NECESSARY
            Path parentPath = path.getParent();
            if (!Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            createObjectMapper().writeValue(Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    toPersistentModel(executionInformationMap));
        } catch (IOException e) {
            throw new JsonPersistenceException("It was not possible to serialize/write to json.", e);
        }
    }

    private JsonPersonalityModel toPersistentModel(Map<Class<?>, ExecutionInformation> executionInformationMap) {
        Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> jointPointBehaviorMap = toJoinPointBehaviorMap(executionInformationMap);

        return new JsonPersonalityModel(UUID.randomUUID().toString(), jointPointBehaviorMap);
    }

    private Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> toJoinPointBehaviorMap(Map<Class<?>, ExecutionInformation> executionInformationMap) {
        Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> joinPointJsonPersistentActualBehaviorMap = new HashMap<>();

        for (Map.Entry<Class<?>, ExecutionInformation> informationEntry : executionInformationMap.entrySet()) {
            ExecutionInformation information = informationEntry.getValue();
            Map<Behavior, BehaviorExecutionInformation> behaviorBehaviorExecutionInformationMap = information.getAll();

            for (Map.Entry<Behavior, BehaviorExecutionInformation> behaviorExecutionInformationEntry : behaviorBehaviorExecutionInformationMap.entrySet()) {
                addToPersistentMap(joinPointJsonPersistentActualBehaviorMap, behaviorExecutionInformationEntry);
            }
        }
        return joinPointJsonPersistentActualBehaviorMap;
    }

    private void addToPersistentMap(Map<JsonPersistentJoinPoint, JsonPersistentActualBehavior> joinPointJsonPersistentActualBehaviorMap,
                                    Map.Entry<Behavior, BehaviorExecutionInformation> behaviorExecutionInformationEntry) {
        Behavior behavior = behaviorExecutionInformationEntry.getKey();
        BehaviorExecutionInformation behaviorExecutionInformation = behaviorExecutionInformationEntry.getValue();

        List<MethodCall> calls = behaviorExecutionInformation.getMethodCalls();

        JsonPersistentJoinPoint persistentJoinPoint = new JsonPersistentJoinPoint(behavior.getBehaviorId());
        JsonPersistentActualBehavior jsonPersistentActualBehavior = new JsonPersistentActualBehavior();

        for (MethodCall call : calls) {
            List<Bean> argsAsBeans = BeanFactory.toBean(call.getArgs());
            Bean returnValueBean = BeanFactory.toBean(call.getReturnValue());
            jsonPersistentActualBehavior.addCall(new JsonPersistentParameter(argsAsBeans),
                    new JsonPersistentReturnValue(returnValueBean));
        }
        joinPointJsonPersistentActualBehaviorMap.put(persistentJoinPoint, jsonPersistentActualBehavior);
    }

}
