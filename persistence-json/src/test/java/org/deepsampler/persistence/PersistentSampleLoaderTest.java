package org.deepsampler.persistence;

import org.deepsampler.core.model.*;
import org.deepsampler.persistence.json.JsonSourceManager;
import org.deepsampler.persistence.json.PersistentSample;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersistentSampleLoaderTest {

    @Test
    public void testSimpleApiLoadWithJson() throws Exception {
        // GIVEN
        SampleDefinition saySample = new SampleDefinition(new SampledMethod(InnerBean.class, InnerBean.class.getDeclaredMethod("say")));
        Path path = Paths.get("./record/testApiSay.json");

        SampleRepository.getInstance().add(saySample);
        ExecutionRepository.getInstance().getOrCreate(InnerBean.class).getOrCreateBySample(saySample).addMethodCall(new MethodCall("HELLO AGAIN"));

        // WHEN
        PersistentSample.source(new JsonSourceManager("./record/testApiSay.json"))
                .record();

        // THEN
        assertTrue(Files.exists(path));
        assertTrue(new String(Files.readAllBytes(path)).replaceAll("\\r", "").endsWith("joinPointBehaviorMap\" : {\n" +
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

    @AfterEach
    public void cleanUp() {
        ExecutionRepository.getInstance().clear();
        SampleRepository.getInstance().clear();
    }

    private static class DeepBean {
        private InnerBean innerBean;

        public String say() {
            return innerBean.say();
        }
    }

    private static class InnerBean {

        public String say() {
            return "Hello";
        }
    }
}
