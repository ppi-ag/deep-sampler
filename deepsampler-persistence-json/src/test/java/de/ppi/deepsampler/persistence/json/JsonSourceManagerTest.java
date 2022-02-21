/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.ppi.deepsampler.core.model.*;
import de.ppi.deepsampler.persistence.PersistentSamplerContext;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentParameter;
import de.ppi.deepsampler.persistence.model.PersistentModel;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonSourceManagerTest {

    @Test
    void testBuilderWithSerializer() throws NoSuchMethodException, IOException {
        // GIVEN
        final Map<Class<?>, ExecutionInformation> executionInformationMap = new HashMap<>();
        final ExecutionInformation executionInformation = new ExecutionInformation();
        final SampledMethod sampledMethod = new SampledMethod(String.class, Bean.class.getDeclaredMethod("testMethod", Object.class));
        final SampleDefinition sampleDefinition = new SampleDefinition(sampledMethod);
        sampleDefinition.setParameterMatchers(Collections.singletonList((p) -> true));
        final SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(sampleDefinition);
        sampleExecutionInformation.addMethodCall(new MethodCall("ABC", Collections.singletonList("BCD")));
        executionInformationMap.put(String.class, executionInformation);
        final String pathAsString = "./abc.json";

        // WHEN
        final JsonSourceManager sourceManager = JsonSourceManager.builder()
                .addDeserializer(JsonPersistentParameter.class, new CustomJsonDeserializer())
                .addSerializer(JsonPersistentParameter.class, new CustomJsonSerializer())
                .buildWithFile(pathAsString);
        sourceManager.save(executionInformationMap, new PersistentSamplerContext());

        // THEN
        Files.exists(Paths.get(pathAsString));
        assertTrue(new String(Files.readAllBytes(Paths.get(pathAsString))).contains("\"myitem\" : 1"));
        Files.delete(Paths.get(pathAsString));
    }

    @Test
    void testBuilderWithModule() throws Exception {
        // GIVEN
        final Map<Class<?>, ExecutionInformation> executionInformationMap = new HashMap<>();
        final ExecutionInformation executionInformation = new ExecutionInformation();
        final SampledMethod sampledMethod = new SampledMethod(String.class, Bean.class.getDeclaredMethod("testMethod", Object.class));
        final SampleDefinition sampleDefinition = new SampleDefinition(sampledMethod);
        sampleDefinition.setParameterMatchers(Collections.singletonList((p) -> true));
        final SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(sampleDefinition);
        sampleExecutionInformation.addMethodCall(new MethodCall("ABC", Collections.singletonList("BCD")));
        executionInformationMap.put(String.class, executionInformation);
        final String pathAsString = "./abc.json";

        // WHEN
        final SimpleModule module = new SimpleModule();
        module.addSerializer(JsonPersistentParameter.class, new CustomJsonSerializer());
        module.addDeserializer(JsonPersistentParameter.class, new CustomJsonDeserializer());
        final JsonSourceManager sourceManager = JsonSourceManager.builder()
                .addModule(module)
                .buildWithFile(pathAsString);
        sourceManager.save(executionInformationMap, new PersistentSamplerContext());

        // THEN
        Files.exists(Paths.get(pathAsString));
        assertTrue(new String(Files.readAllBytes(Paths.get(pathAsString))).contains("\"myitem\" : 1"));
        Files.delete(Paths.get(pathAsString));
    }

    @Test
    void testBuilderWithSerializerLoad() {
        // GIVEN
        final String pathAsString = "./record/testPersistent.json";

        // WHEN
        final SimpleModule module = new SimpleModule();
        module.addSerializer(JsonPersistentParameter.class, new CustomJsonSerializer());
        module.addDeserializer(JsonPersistentParameter.class, new CustomJsonDeserializer());
        final JsonSourceManager sourceManager = JsonSourceManager.builder()
                .addModule(module)
                .buildWithFile(pathAsString);
        final PersistentModel persistentModel = sourceManager.load();

        // THEN
        assertNull(persistentModel.getSampleMethodToSampleMap().entrySet().iterator().next()
                .getValue().getAllCalls().get(0).getPersistentParameter().getParameter());
    }

    @Test
    void testBuilderWithModLoad() {
        // GIVEN
        final String pathAsString = "./record/testPersistent.json";

        // WHEN
        final JsonSourceManager sourceManager = JsonSourceManager.builder()
                .addDeserializer(JsonPersistentParameter.class, new CustomJsonDeserializer())
                .addSerializer(JsonPersistentParameter.class, new CustomJsonSerializer())
                .buildWithFile(pathAsString);
        final PersistentModel persistentModel = sourceManager.load();

        // THEN
        assertNull(persistentModel.getSampleMethodToSampleMap().entrySet().iterator().next()
                .getValue().getAllCalls().get(0).getPersistentParameter().getParameter());
    }

    @Test
    void testBuilderWithClassPathResource() {
        // WHEN
        final JsonSourceManager sourceManager = JsonSourceManager.builder()
                .buildWithResource(new PersistentClassPathResource("myTestJson.json", getClass()));
        final PersistentModel persistentModel = sourceManager.load();

        // THEN
        assertNotNull(persistentModel.getSampleMethodToSampleMap().entrySet().iterator().next()
                .getValue().getAllCalls().get(0).getPersistentParameter().getParameter());
    }

    private static class CustomJsonDeserializer extends JsonDeserializer<JsonPersistentParameter> {

        @Override
        public JsonPersistentParameter deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            p.getCodec().readTree(p);
            return new JsonPersistentParameter();
        }
    }

    private static class CustomJsonSerializer extends JsonSerializer<JsonPersistentParameter> {

        @Override
        public void serialize(final JsonPersistentParameter value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("myitem", 1);
            gen.writeEndObject();
        }
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
