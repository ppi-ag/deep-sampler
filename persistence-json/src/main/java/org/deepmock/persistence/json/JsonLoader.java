package org.deepmock.persistence.json;

import org.deepmock.core.api.Matchers;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.ParameterMatcher;
import org.deepmock.persistence.json.error.JsonPersistenceException;
import org.deepmock.persistence.json.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonLoader extends AbstractJsonOperator {
    private final Path path;

    public JsonLoader(Path path) {
        this.path = path;
    }

    public List<Behavior> load() {
        try {
            JsonPersonalityModel model = createObjectMapper().readValue(Files.newBufferedReader(path), JsonPersonalityModel.class);

            return toBehaviors(model);
        } catch (IOException e) {
            throw new JsonPersistenceException("It was not possible to deserialize/read the file", e);
        }
    }

    private List<Behavior> toBehaviors(JsonPersonalityModel model) {
        List<Behavior> behaviors = new ArrayList<>();

        for (Map.Entry<JsonPersistentJoinPoint, JsonPersistentActualBehavior> joinPointBehaviorEntry : model.getJoinPointBehaviorMap().entrySet()) {
            JsonPersistentJoinPoint jsonPersistentJoinPoint = joinPointBehaviorEntry.getKey();
            JsonPersistentActualBehavior jsonPersistentActualBehavior = joinPointBehaviorEntry.getValue();

            for (JsonPersistentMethodCall call : jsonPersistentActualBehavior.getAllCalls()) {
                Behavior behavior = mapToBehavior(jsonPersistentJoinPoint, call);
                behaviors.add(behavior);
            }
        }
        return behaviors;
    }

    private Behavior mapToBehavior(JsonPersistentJoinPoint jsonPersistentJoinPoint, JsonPersistentMethodCall call) {
        // JOINT POINT IS EMPTY AT THE START, ONLY IF JOINPOINT GOT DEFINED BY org.deepmock.core.api.Personality
        // THE LOADED BEHAVIOR IS VALID
        Behavior behavior = new Behavior(null);
        behavior.setBehaviorId(jsonPersistentJoinPoint.getJoinPointId());
        behavior.setParameter(toMatcher(call.getJsonPersistentParameter()));
        behavior.setReturnValueSupplier(() -> call.getJsonPersistentReturnValue().getReturnValue());
        return behavior;
    }

    private List<ParameterMatcher> toMatcher(JsonPersistentParameter jsonPersistentParameter) {
        return jsonPersistentParameter.getArgs().stream()
                .map(arg -> Matchers.equalTo(arg))
                .collect(Collectors.toList());
    }
}
