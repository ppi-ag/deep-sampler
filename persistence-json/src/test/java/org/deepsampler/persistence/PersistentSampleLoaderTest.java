package org.deepsampler.persistence;

import org.deepsampler.core.api.Sample;
import org.deepsampler.core.api.Sampler;
import org.deepsampler.core.model.*;
import org.deepsampler.persistence.json.JsonSourceManager;
import org.deepsampler.persistence.json.PersistentSample;
import org.deepsampler.persistence.json.PersistentSampleLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class PersistentSampleLoaderTest {

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
        PersistentSample.source(new JsonSourceManager("./record/testApiSay.json"))
                .record();

        // THEN
        assertTrue(Files.exists(path));

        String jsonFromFile = new String(Files.readAllBytes(path)).replaceAll("\\r", "");
        assertTrue(jsonFromFile.endsWith("joinPointBehaviorMap\" : {\n" +
                "    \"public java.lang.String org.deepsampler.persistence.PersistentSampleLoaderTest$InnerBean.say()\" : {\n" +
                "      \"callMap\" : [ {\n" +
                "        \"parameter\" : {\n" +
                "          \"args\" : [ ]\n" +
                "        },\n" +
                "        \"returnValue\" : {\n" +
                "          \"returnValue\" : \"HELLO AGAIN\"\n" +
                "        }\n" +
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
        PersistentSample.source(new JsonSourceManager("./record/testApiSayPersistent.json"))
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
