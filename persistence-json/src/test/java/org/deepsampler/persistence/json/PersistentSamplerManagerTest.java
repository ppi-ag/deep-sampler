package org.deepsampler.persistence.json;

import org.deepsampler.core.model.*;
import org.deepsampler.persistence.api.PersistentSampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentSamplerManagerTest {

    @Test
    void testSimpleApiRecord() throws Exception {
        // GIVEN
        final SampleDefinition saySample = new SampleDefinition(new SampledMethod(InnerBean.class, InnerBean.class.getDeclaredMethod("say")));
        final Path path = Paths.get("./record/testApiSay.json");

        SampleRepository.getInstance().add(saySample);
        ExecutionRepository.getInstance()
                .getOrCreate(InnerBean.class)
                .getOrCreateBySample(saySample)
                .addMethodCall(new MethodCall("HELLO AGAIN", null));

        // WHEN
        PersistentSampler.source(JsonSourceManager.builder("./record/testApiSay.json").build())
                .record();

        // THEN
        assertTrue(Files.exists(path));
        assertTrue(new String(Files.readAllBytes(path)).replaceAll("\\r", "").endsWith("sampleMethodToSampleMap\" : {\n" +
                "    \"public java.lang.String org.deepsampler.persistence.json.PersistentSamplerManagerTest$InnerBean.say()\" : {\n" +
                "      \"callMap\" : [ {\n" +
                "        \"parameter\" : {\n" +
                "          \"args\" : [ ]\n" +
                "        },\n" +
                "        \"returnValue\" : \"HELLO AGAIN\"\n" +
                "      } ]\n" +
                "    }\n" +
                "  }\n" +
                "}"));

        Files.delete(path);
    }

    @Test
    void testSimpleLoad() throws Exception {
        // GIVEN
        final SampleDefinition saySample = new SampleDefinition(new SampledMethod(InnerBean.class, InnerBean.class.getDeclaredMethod("say")));
        SampleRepository.getInstance().add(saySample);

        // WHEN
        PersistentSampler.source(JsonSourceManager.builder("./record/testApiSayPersistent.json").build())
                .load();

        // THEN
        assertEquals(1, SampleRepository.getInstance().getSamples().size());
        assertEquals("HELLO AGAIN", SampleRepository.getInstance().getSamples().get(0).getReturnValueSupplier().supply());
    }

    @AfterEach
    void cleanUp() {
        ExecutionRepository.getInstance().clear();
        SampleRepository.getInstance().clear();
    }

    private static class InnerBean {

        public String say() {
            return "Hello";
        }
    }

}
