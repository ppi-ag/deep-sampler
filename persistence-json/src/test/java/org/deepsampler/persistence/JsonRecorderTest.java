package org.deepsampler.persistence;

import org.deepsampler.persistence.json.JsonRecorder;
import org.deepsampler.core.internal.api.ExecutionManager;
import org.deepsampler.core.model.ExecutionRepository;
import org.deepsampler.core.model.MethodCall;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampledMethod;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonRecorderTest {

    @Test
    public void testRecord() throws Exception {
        // GIVEN
        Path path = Paths.get("./record/testTemp.json");
        SampleDefinition behavior = new SampleDefinition(new SampledMethod(getClass(), getClass().getMethod("testRecord")));
        behavior.setBehaviorId("TestMethodForRecord");
        ExecutionManager.log(behavior, new MethodCall("ABC", "Args1"));
        ExecutionManager.log(behavior, new MethodCall(new Bean("ABC", "ABC"), "Args1"));

        // WHEN
        new JsonRecorder(path).record(ExecutionRepository.getInstance().getAll());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @Test
    public void testRecordLocalDateTime() throws Exception {
        // GIVEN
        Path path = Paths.get("./record/testTimeTemp.json");
        SampleDefinition behavior = new SampleDefinition(new SampledMethod(getClass(), getClass().getMethod("testRecord")));
        behavior.setBehaviorId("TestMethodForRecord");
        ExecutionManager.log(behavior, new MethodCall("ABC", LocalDateTime.now()));
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
