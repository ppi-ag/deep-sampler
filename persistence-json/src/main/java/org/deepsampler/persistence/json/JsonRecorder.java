package org.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.Module;
import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.core.model.MethodCall;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleExecutionInformation;
import org.deepsampler.persistence.json.bean.PersistentBeanFactory;
import org.deepsampler.persistence.json.error.JsonPersistenceException;
import org.deepsampler.persistence.json.extension.SerializationExtension;
import org.deepsampler.persistence.json.model.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class JsonRecorder extends JsonOperator {

    public JsonRecorder(Path pathToJson) {
        super(pathToJson, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public JsonRecorder(Path pathToJson, List<SerializationExtension<?>> serializationExtensions, List<Module> moduleList) {
        super(pathToJson, Collections.emptyList(), serializationExtensions, moduleList);
    }

    public void record(final Map<Class<?>, ExecutionInformation> executionInformationMap) {
        try {
            // CREATE PARENT DIR IF NECESSARY
            final Path parentPath = getPath().getParent();
            if (!Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }

            final JsonSampleModel model = toPersistentModel(executionInformationMap);
            final BufferedWriter writer = Files.newBufferedWriter(getPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            createObjectMapper().writeValue(writer, model);
        } catch (final IOException e) {
            throw new JsonPersistenceException("It was not possible to serialize/write to json.", e);
        }
    }

    private JsonSampleModel toPersistentModel(final Map<Class<?>, ExecutionInformation> executionInformationMap) {
        final Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodToSample = toSampleMethodSampleMap(executionInformationMap);

        return new JsonSampleModel(UUID.randomUUID().toString(), sampleMethodToSample);
    }

    private Map<JsonPersistentSampleMethod, JsonPersistentActualSample> toSampleMethodSampleMap(final Map<Class<?>, ExecutionInformation> executionInformationMap) {
        final Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodJsonPersistentActualSampleMap = new HashMap<>();

        for (final Map.Entry<Class<?>, ExecutionInformation> informationEntry : executionInformationMap.entrySet()) {
            final ExecutionInformation information = informationEntry.getValue();
            final Map<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationMap = information.getAll();

            for (final Map.Entry<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationEntry : sampleExecutionInformationMap.entrySet()) {
                addToPersistentMap(sampleMethodJsonPersistentActualSampleMap, sampleExecutionInformationEntry);
            }
        }
        return sampleMethodJsonPersistentActualSampleMap;
    }

    private void addToPersistentMap(final Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodJsonPersistentActualSampleMap,
                                    final Map.Entry<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationEntry) {
        final SampleDefinition sample = sampleExecutionInformationEntry.getKey();
        final SampleExecutionInformation sampleExecutionInformation = sampleExecutionInformationEntry.getValue();

        final List<MethodCall> calls = sampleExecutionInformation.getMethodCalls();

        final JsonPersistentSampleMethod persistentSampleMethod = new JsonPersistentSampleMethod(sample.getSampleId());
        final JsonPersistentActualSample jsonPersistentActualSample = new JsonPersistentActualSample();

        for (final MethodCall call : calls) {
            final List<Object> argsAsPersistentBeans = PersistentBeanFactory.toBeanIfNecessary(call.getArgs());
            final Object returnValuePersistentBean = PersistentBeanFactory.toBeanIfNecessary(call.getReturnValue());
            jsonPersistentActualSample.addCall(new JsonPersistentParameter(argsAsPersistentBeans),
                    new JsonPersistentReturnValue(returnValuePersistentBean));
        }
        sampleMethodJsonPersistentActualSampleMap.put(persistentSampleMethod, jsonPersistentActualSample);
    }

}
