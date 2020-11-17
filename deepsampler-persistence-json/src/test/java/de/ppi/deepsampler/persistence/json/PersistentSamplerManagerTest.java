/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import de.ppi.deepsampler.core.model.*;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
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
        PersistentSampler.source(JsonSourceManager.builder().buildWithFile("./record/testApiSay.json"))
                .record();

        // THEN
        assertTrue(Files.exists(path));
        assertTrue(new String(Files.readAllBytes(path)).replaceAll("\\r", "").endsWith("sampleMethodToSampleMap\" : {\n" +
                "    \"public java.lang.String de.ppi.deepsampler.persistence.json.PersistentSamplerManagerTest$InnerBean.say()\" : {\n" +
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
        PersistentSampler.source(JsonSourceManager.builder().buildWithFile("./record/testApiSayPersistent.json"))
                .load();

        // THEN
        assertEquals(1, SampleRepository.getInstance().getSamples().size());
        assertEquals("HELLO AGAIN", SampleRepository.getInstance().getSamples().get(0).getAnswer().call(null));
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
        PersistentSampler.source(JsonSourceManager.builder().buildWithFile(path.toString()))
                .record();

        // THEN
        assertTrue(Files.exists(path));
        assertTrue(new String(Files.readAllBytes(path)).replaceAll("\\r", "").endsWith("sampleMethodToSampleMap\" : {\n" +
                "    \"public java.time.LocalDateTime de.ppi.deepsampler.persistence.json.PersistentSamplerManagerTest$DateBean.now()\" : {\n" +
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
        PersistentSampler.source(JsonSourceManager.builder().buildWithFile("./record/testApiDatePersistent.json"))
                .load();

        // THEN
        assertEquals(1, SampleRepository.getInstance().getSamples().size());
        assertEquals(LocalDateTime.of(2019, 2, 2, 2, 2), SampleRepository.getInstance().getSamples().get(0).getAnswer().call(null));
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
