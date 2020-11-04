package org.deepsampler.persistence.json;

import org.deepsampler.core.model.*;
import org.deepsampler.persistence.api.PersistentSampler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

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
        PersistentSampler.source(JsonSourceManager.builderWithFile("./record/testApiSay.json").build())
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
        PersistentSampler.source(JsonSourceManager.builderWithFile("./record/testApiSayPersistent.json").build())
                .load();

        // THEN
        assertEquals(1, SampleRepository.getInstance().getSamples().size());
        assertEquals("HELLO AGAIN", SampleRepository.getInstance().getSamples().get(0).getReturnValueSupplier().supply());
    }

    @Test
    void testDateRecord() throws Exception {
        // GIVEN
        final SampleDefinition dateSample = new SampleDefinition(new SampledMethod(DateBean.class, DateBean.class.getDeclaredMethod("now")));
        final Path path = Paths.get("./record/testApiDate.json");
        ExecutionRepository.getInstance()
                .getOrCreate(InnerBean.class)
                .getOrCreateBySample(dateSample)
                .addMethodCall(new MethodCall(LocalDateTime.of(2019, 2 ,2 ,2 ,2), null));

        // WHEN
        PersistentSampler.source(JsonSourceManager.builderWithFile("./record/testApiDate.json").build())
                .record();

        // THEN
        assertTrue(Files.exists(path));
        assertTrue(new String(Files.readAllBytes(path)).replaceAll("\\r", "").endsWith("sampleMethodToSampleMap\" : {\n" +
                "    \"public java.time.LocalDateTime org.deepsampler.persistence.json.PersistentSamplerManagerTest$DateBean.now()\" : {\n" +
                "      \"callMap\" : [ {\n" +
                "        \"parameter\" : {\n" +
                "          \"args\" : [ ]\n" +
                "        },\n" +
                "        \"returnValue\" : [ \"java.time.LocalDateTime\", [ 2019, 2, 2, 2, 2 ] ]\n" +
                "      } ]\n" +
                "    }\n" +
                "  }\n" +
                "}"));

        Files.delete(path);
    }

    @Test
    void testDateLoad() throws Exception {
        // GIVEN
        final SampleDefinition dateSample = new SampleDefinition(new SampledMethod(DateBean.class, DateBean.class.getDeclaredMethod("now")));
        SampleRepository.getInstance().add(dateSample);

        // WHEN
        PersistentSampler.source(JsonSourceManager.builderWithFile("./record/testApiDatePersistent.json").build())
                .load();

        // THEN
        assertEquals(1, SampleRepository.getInstance().getSamples().size());
        assertEquals(LocalDateTime.of(2019, 2, 2, 2, 2), SampleRepository.getInstance().getSamples().get(0).getReturnValueSupplier().supply());
    }

    @AfterEach
    void cleanUp() {
        ExecutionRepository.getInstance().clear();
        SampleRepository.getInstance().clear();
    }

    private static class DateBean {

        public LocalDateTime now() {
            return LocalDateTime.of(2020, 2, 2, 1, 1);
        }
    }

    private static class InnerBean {

        public String say() {
            return "Hello";
        }
    }

}
