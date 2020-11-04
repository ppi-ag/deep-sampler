package org.deepsampler.persistence.json;

import org.deepsampler.core.internal.api.ExecutionManager;
import org.deepsampler.core.model.*;
import org.deepsampler.persistence.PersistentSamplerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRecorderTest {

    @Test
    void testRecord() throws Exception {
        // GIVEN
        final Path path = Paths.get("./record/testTemp.json");

        final SampleDefinition sample = new SampleDefinition(new SampledMethod(Bean.class, Bean.class.getMethod("testMethod")));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.record(sample, new MethodCall("ABC", Arrays.asList("Args1")));
        ExecutionManager.record(sample, new MethodCall(new Bean("ABC", "ABC"), Arrays.asList("Args1")));

        // WHEN
        new JsonRecorder(path).record(ExecutionRepository.getInstance().getAll(), new PersistentSamplerContext());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @Test
    void testRecordLocalDateTime() throws Exception {
        // GIVEN
        final Path path = Paths.get("./record/testTimeTemp.json");
        final SampleDefinition sample = new SampleDefinition(new SampledMethod(Bean.class, Bean.class.getMethod("testMethod")));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.record(sample, new MethodCall("ABC", Arrays.asList(LocalDateTime.now())));
        ExecutionManager.record(sample, new MethodCall(new Bean("ABC", "ABC"), Arrays.asList("Args1")));

        // WHEN
        new JsonRecorder(path).record(ExecutionRepository.getInstance().getAll(), new PersistentSamplerContext());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @AfterEach
    void cleanUp() {
        ExecutionRepository.getInstance().clear();
        SampleRepository.getInstance().clear();
    }

    private static class Bean {
        private String valueOne;
        private String valueTwo;

        @SuppressWarnings("unused")
        public Bean() {
            // DEFAULT FOR JACKSON
        }

        public Bean(final String valueOne, final String valueTwo) {
            this.valueOne = valueOne;
            this.valueTwo = valueTwo;
        }

        @SuppressWarnings("unused")
        public void testMethod() {
            // Is used by reflection only is not intended to do anything.
        }
    }
}
