package de.ppi.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.Module;
import de.ppi.deepsampler.core.model.ExecutionInformation;
import de.ppi.deepsampler.core.model.MethodCall;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleExecutionInformation;
import de.ppi.deepsampler.persistence.PersistentSamplerContext;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentActualSample;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentParameter;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentSampleMethod;
import de.ppi.deepsampler.persistence.json.error.JsonPersistenceException;
import de.ppi.deepsampler.persistence.json.extension.SerializationExtension;
import de.ppi.deepsampler.persistence.json.model.JsonSampleModel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class JsonRecorder extends JsonOperator {

    public JsonRecorder(PersistentResource persistentResource) {
        super(persistentResource, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public JsonRecorder(PersistentResource persistentResource, List<SerializationExtension<?>> serializationExtensions, List<Module> moduleList) {
        super(persistentResource, Collections.emptyList(), serializationExtensions, moduleList);
    }

    public void record(final Map<Class<?>, ExecutionInformation> executionInformationMap, PersistentSamplerContext persistentSamplerContext) {
        try {
            final PersistentResource persistentResource = getPersistentResource();
            if (persistentResource instanceof PersistentFile) {
                // CREATE PARENT DIR IF NECESSARY
                final Path parentPath = ((PersistentFile) persistentResource).getFilePath().getParent();
                if (!Files.exists(parentPath)) {
                    Files.createDirectories(parentPath);
                }
            }

            final JsonSampleModel model = toPersistentModel(executionInformationMap, persistentSamplerContext);
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(persistentResource.writeAsStream(StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE)));
            createObjectMapper().writeValue(writer, model);
        } catch (final IOException e) {
            throw new JsonPersistenceException("It was not possible to serialize/write to json.", e);
        }
    }

    private JsonSampleModel toPersistentModel(final Map<Class<?>, ExecutionInformation> executionInformationMap, PersistentSamplerContext persistentSamplerContext) {
        final Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodToSample = toSampleMethodSampleMap(executionInformationMap, persistentSamplerContext);

        return new JsonSampleModel(UUID.randomUUID().toString(), sampleMethodToSample);
    }

    private Map<JsonPersistentSampleMethod, JsonPersistentActualSample> toSampleMethodSampleMap(final Map<Class<?>, ExecutionInformation> executionInformationMap,
                                                                                                PersistentSamplerContext persistentSamplerContext) {
        final Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodJsonPersistentActualSampleMap = new HashMap<>();

        for (final Map.Entry<Class<?>, ExecutionInformation> informationEntry : executionInformationMap.entrySet()) {
            final ExecutionInformation information = informationEntry.getValue();
            final Map<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationMap = information.getAll();

            for (final Map.Entry<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationEntry : sampleExecutionInformationMap.entrySet()) {
                addToPersistentMap(sampleMethodJsonPersistentActualSampleMap, sampleExecutionInformationEntry, persistentSamplerContext);
            }
        }
        return sampleMethodJsonPersistentActualSampleMap;
    }

    private void addToPersistentMap(final Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodJsonPersistentActualSampleMap,
                                    final Map.Entry<SampleDefinition, SampleExecutionInformation> sampleExecutionInformationEntry,
                                    PersistentSamplerContext persistentSamplerContext) {
        final SampleDefinition sample = sampleExecutionInformationEntry.getKey();
        final SampleExecutionInformation sampleExecutionInformation = sampleExecutionInformationEntry.getValue();

        final List<MethodCall> calls = sampleExecutionInformation.getMethodCalls();

        final JsonPersistentSampleMethod persistentSampleMethod = new JsonPersistentSampleMethod(sample.getSampleId());
        final JsonPersistentActualSample jsonPersistentActualSample = new JsonPersistentActualSample();

        for (final MethodCall call : calls) {
            final List<Object> argsAsPersistentBeans = persistentSamplerContext.getPersistentBeanFactory().toBeanIfNecessary(call.getArgs());
            final Object returnValuePersistentBean = persistentSamplerContext.getPersistentBeanFactory().toBeanIfNecessary(call.getReturnValue());
            jsonPersistentActualSample.addCall(new JsonPersistentParameter(argsAsPersistentBeans),
                    returnValuePersistentBean);
        }
        sampleMethodJsonPersistentActualSampleMap.put(persistentSampleMethod, jsonPersistentActualSample);
    }

}
