package org.deepsampler.persistence.json;

import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.core.model.MethodCall;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleExecutionInformation;
import org.deepsampler.persistence.json.bean.PersistentBeanFactory;
import org.deepsampler.persistence.json.error.JsonPersistenceException;
import org.deepsampler.persistence.json.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonRecorder extends JsonOperator {
    private final Path path;

    public JsonRecorder(Path path) {
        this.path = path;
    }

    public void record(Map<Class<?>, ExecutionInformation> executionInformationMap) {
        try {
            // CREATE PARENT DIR IF NECESSARY
            Path parentPath = path.getParent();
            if (!Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            createObjectMapper().writeValue(Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    toPersistentModel(executionInformationMap));
        } catch (IOException e) {
            throw new JsonPersistenceException("It was not possible to serialize/write to json.", e);
        }
    }

    private JsonSampleModel toPersistentModel(Map<Class<?>, ExecutionInformation> executionInformationMap) {
        Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodToSample = toSampleMethodSampleMap(executionInformationMap);

        return new JsonSampleModel(UUID.randomUUID().toString(), sampleMethodToSample);
    }

    private Map<JsonPersistentSampleMethod, JsonPersistentActualSample> toSampleMethodSampleMap(Map<Class<?>, ExecutionInformation> executionInformationMap) {
        Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodJsonPersistentActualSampleMap = new HashMap<>();

        for (Map.Entry<Class<?>, ExecutionInformation> informationEntry : executionInformationMap.entrySet()) {
            ExecutionInformation information = informationEntry.getValue();
            Map<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationMap = information.getAll();

            for (Map.Entry<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationEntry : sampleExecutionInformationMap.entrySet()) {
                addToPersistentMap(sampleMethodJsonPersistentActualSampleMap, sampleExecutionInformationEntry);
            }
        }
        return sampleMethodJsonPersistentActualSampleMap;
    }

    private void addToPersistentMap(Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodJsonPersistentActualSampleMap,
                                    Map.Entry<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationEntry) {
        SampleDefinition sample = sampleExecutionInformationEntry.getKey();
        SampleExecutionInformation sampleExecutionInformation = sampleExecutionInformationEntry.getValue();

        List<MethodCall> calls = sampleExecutionInformation.getMethodCalls();

        JsonPersistentSampleMethod persistentSampleMethod = new JsonPersistentSampleMethod(sample.getSampleId());
        JsonPersistentActualSample jsonPersistentActualSample = new JsonPersistentActualSample();

        for (MethodCall call : calls) {
            List<Object> argsAsPersistentBeans = PersistentBeanFactory.toBeanIfNecessary(call.getArgs());
            Object returnValuePersistentBean = PersistentBeanFactory.toBeanIfNecessary(call.getReturnValue());
            jsonPersistentActualSample.addCall(new JsonPersistentParameter(argsAsPersistentBeans),
                    new JsonPersistentReturnValue(returnValuePersistentBean));
        }
        sampleMethodJsonPersistentActualSampleMap.put(persistentSampleMethod, jsonPersistentActualSample);
    }

}
