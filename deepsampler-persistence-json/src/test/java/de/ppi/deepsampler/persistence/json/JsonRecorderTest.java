/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import de.ppi.deepsampler.core.internal.api.ExecutionManager;
import de.ppi.deepsampler.core.model.*;
import de.ppi.deepsampler.persistence.PersistentSamplerContext;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentSampleMethod;
import de.ppi.deepsampler.persistence.model.PersistentActualSample;
import de.ppi.deepsampler.persistence.model.PersistentModel;
import de.ppi.deepsampler.persistence.model.PersistentSampleMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRecorderTest {

    @Test
    void testRecord() throws Exception {
        // GIVEN
        final Path path = Paths.get("./record/testTemp.json");

        final SampleDefinition sample = new SampleDefinition(new SampledMethod(Bean.class, Bean.class.getMethod("testMethod", Object.class)));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.recordMethodCall(sample, new MethodCall("ABC", Collections.singletonList("Args1")));
        ExecutionManager.recordMethodCall(sample, new MethodCall(new Bean("ABC", "ABC"), Collections.singletonList("Args1")));

        // WHEN
        new JsonRecorder(new PersistentFile(path), StandardCharsets.UTF_8)
                .recordExecutionInformation(ExecutionRepository.getInstance().getAll(), new PersistentSamplerContext());

        // THEN
        assertTrue(Files.exists(path));

        Files.delete(path);
    }

    @Test
    void doesNotRecordRedundantCalls() throws Exception {
        // GIVEN
        final Path path = Paths.get("./record/testTemp.json");

        final SampleDefinition sample = new SampleDefinition(new SampledMethod(Bean.class, Bean.class.getMethod("testMethod", Object.class)));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.recordMethodCall(sample, new MethodCall("ABC", Collections.singletonList("Args1")));
        ExecutionManager.recordMethodCall(sample, new MethodCall("ABC", Collections.singletonList("Args1")));

        // WHEN
        new JsonRecorder(new PersistentFile(path), StandardCharsets.UTF_8)
                .recordExecutionInformation(ExecutionRepository.getInstance().getAll(), new PersistentSamplerContext());

        // THEN
        assertTrue(Files.exists(path));

        final PersistentModel loadedModel = new JsonLoader(new PersistentFile(path), StandardCharsets.UTF_8).load();
        Map<PersistentSampleMethod, PersistentActualSample> sampleMap = loadedModel.getSampleMethodToSampleMap();
        PersistentActualSample persistentActualSample = sampleMap.get(new JsonPersistentSampleMethod("TestMethodForRecord"));

        assertEquals(1, sampleMap.size());
        assertEquals(1, persistentActualSample.getAllCalls().size());

        Files.delete(path);
    }

    @Test
    void testRecordLocalDateTime() throws Exception {
        // GIVEN
        final Path path = Paths.get("./record/testTimeTemp.json");
        final SampleDefinition sample = new SampleDefinition(new SampledMethod(Bean.class, Bean.class.getMethod("testMethod", Object.class)));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.recordMethodCall(sample, new MethodCall("ABC", Collections.singletonList(LocalDateTime.now())));
        ExecutionManager.recordMethodCall(sample, new MethodCall(new Bean("ABC", "ABC"), Collections.singletonList("Args1")));

        // WHEN
        new JsonRecorder(new PersistentFile(path), StandardCharsets.UTF_8)
                .recordExecutionInformation(ExecutionRepository.getInstance().getAll(), new PersistentSamplerContext());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @Test
    void testRecordLocalDateTimeWithoutParent() throws Exception {
        // GIVEN
        final Path path = Paths.get("testTimeTemp.json");
        final SampleDefinition sample = new SampleDefinition(new SampledMethod(Bean.class, Bean.class.getMethod("testMethod", Object.class)));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.recordMethodCall(sample, new MethodCall("ABC", Collections.singletonList(LocalDateTime.now())));
        ExecutionManager.recordMethodCall(sample, new MethodCall(new Bean("ABC", "ABC"), Collections.singletonList("Args1")));

        // WHEN
        new JsonRecorder(new PersistentFile(path), StandardCharsets.UTF_8)
                .recordExecutionInformation(ExecutionRepository.getInstance().getAll(), new PersistentSamplerContext());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @Test
    void testWithComplicatedCollections() throws Exception {
        // GIVEN
        CollectionBean bean = new CollectionBean();
        bean.stringCollection = new ArrayList<>();
        bean.stringCollection.add("AC");
        final Path path = Paths.get("testTimeTemp.json");
        final SampleDefinition sample = new SampleDefinition(new SampledMethod(Bean.class, Bean.class.getMethod("testMethod", Object.class)));
        sample.setSampleId("TestMethodForRecord");
        ExecutionManager.recordMethodCall(sample, new MethodCall("ABC", Collections.singletonList(bean)));
        ExecutionManager.recordMethodCall(sample, new MethodCall(new Bean("ABC", "ABC"), Collections.singletonList("Args1")));

        // WHEN
        new JsonRecorder(new PersistentFile(path), StandardCharsets.UTF_8)
                .recordExecutionInformation(ExecutionRepository.getInstance().getAll(), new PersistentSamplerContext());

        // THEN
        assertTrue(Files.exists(path));
        Files.delete(path);
    }

    @AfterEach
    void cleanUp() {
        ExecutionRepository.getInstance().clear();
        SampleRepository.getInstance().clear();
    }

    private static class CollectionBean {
        private Collection<Object> stringCollection;
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
        public Object testMethod(Object obj) {
            // Is used by reflection only is not intended to do anything.
            return null;
        }
    }
}
