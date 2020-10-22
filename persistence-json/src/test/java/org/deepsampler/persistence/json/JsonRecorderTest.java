package org.deepsampler.persistence.json;

import org.deepsampler.core.internal.api.ExecutionManager;
import org.deepsampler.core.model.*;
import org.junit.jupiter.api.AfterEach;
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
        final Path path = Paths.get("./record/testTemp.json");
        final SampleDefinition sample = new SampleDefinition(new SampledMethod(getClass(), getClass().getMethod("testRecord")));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.record(sample, new MethodCall("ABC", "Args1"));
        ExecutionManager.record(sample, new MethodCall(new Bean("ABC", "ABC"), "Args1"));

        // WHEN
        new JsonRecorder(path).record(ExecutionRepository.getInstance().getAll());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @Test
    public void testRecordLocalDateTime() throws Exception {
        // GIVEN
        final Path path = Paths.get("./record/testTimeTemp.json");
        final SampleDefinition sample = new SampleDefinition(new SampledMethod(getClass(), getClass().getMethod("testRecord")));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.record(sample, new MethodCall("ABC", LocalDateTime.now()));
        ExecutionManager.record(sample, new MethodCall(new Bean("ABC", "ABC"), "Args1"));

        // WHEN
        new JsonRecorder(path).record(ExecutionRepository.getInstance().getAll());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @AfterEach
    public void cleanUp() {
        ExecutionRepository.getInstance().clear();
        SampleRepository.getInstance().clear();
    }

    private static class Bean {
        private String valueOne;
        private String valueTwo;

        public Bean() {
            // DEFAULT FOR JACKSON
        }

        public Bean(final String valueOne, final String valueTwo) {
            this.valueOne = valueOne;
            this.valueTwo = valueTwo;
        }
    }
}
