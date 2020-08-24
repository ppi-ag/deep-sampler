package org.deepmock.persistence;

import org.deepmock.core.internal.api.ExecutionManager;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.ExecutionRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.MethodCall;
import org.deepmock.persistence.json.JsonRecorder;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonRecorderTest {

    @Test
    public void testRecord() throws Exception {
        // GIVEN
        Path path = Paths.get("./record/testTemp.json");
        Behavior behavior = new Behavior(new JoinPoint(getClass(), getClass().getMethod("testRecord")));
        behavior.setBehaviorId("TestMethodForRecord");
        ExecutionManager.log(behavior, new MethodCall("ABC", "Args1"));
        ExecutionManager.log(behavior, new MethodCall(new Bean("ABC", "ABC"), "Args1"));

        // WHEN
        new JsonRecorder(path).record(ExecutionRepository.getInstance().getAll());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    private static class Bean {
        private String valueOne;
        private String valueTwo;

        public Bean() {
            // DEFAULT FOR JACKSON
        }

        public Bean(String valueOne, String valueTwo) {
            this.valueOne = valueOne;
            this.valueTwo = valueTwo;
        }
    }
}
