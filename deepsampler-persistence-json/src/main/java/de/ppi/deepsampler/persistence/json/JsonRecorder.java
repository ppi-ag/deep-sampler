/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.Module;
import de.ppi.deepsampler.core.model.ExecutionInformation;
import de.ppi.deepsampler.core.model.MethodCall;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleExecutionInformation;
import de.ppi.deepsampler.persistence.PersistentSamplerContext;
import de.ppi.deepsampler.persistence.error.PersistenceException;
import de.ppi.deepsampler.persistence.json.extension.SerializationExtension;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentActualSample;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentParameter;
import de.ppi.deepsampler.persistence.json.model.JsonPersistentSampleMethod;
import de.ppi.deepsampler.persistence.json.model.JsonSampleModel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class JsonRecorder extends JsonOperator {

    public JsonRecorder(PersistentResource persistentResource, Charset charset) {
        super(persistentResource, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), charset);
    }

    public JsonRecorder(PersistentResource persistentResource, List<SerializationExtension<?>> serializationExtensions, List<Module> moduleList, Charset charset) {
        super(persistentResource, Collections.emptyList(), serializationExtensions, moduleList, charset);
    }

    public void recordExecutionInformation(final Map<Class<?>, ExecutionInformation> executionInformationMap, PersistentSamplerContext persistentSamplerContext) {
        try {
            final PersistentResource persistentResource = getPersistentResource();
            if (persistentResource instanceof PersistentFile) {
                // CREATE PARENT DIR IF NECESSARY
                final Path parentPath = ((PersistentFile) persistentResource).getFilePath().getParent();
                if (parentPath != null && !Files.exists(parentPath)) {
                    Files.createDirectories(parentPath);
                }
            }

            final JsonSampleModel model = toPersistentModel(executionInformationMap, persistentSamplerContext);

            final OutputStream outputStream = persistentResource.writeAsStream(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, getCharset()));

            createObjectMapper().writeValue(writer, model);
        } catch (final IOException e) {
            throw new PersistenceException("It was not possible to serialize/write to json.", e);
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
            final List<SampleExecutionInformation> sampleExecutionInformationMapList = information.getAll();

            for (final SampleExecutionInformation sampleExecutionInformation : sampleExecutionInformationMapList) {
                addToPersistentMap(sampleMethodJsonPersistentActualSampleMap, sampleExecutionInformation, persistentSamplerContext);
            }
        }
        return sampleMethodJsonPersistentActualSampleMap;
    }

    private void addToPersistentMap(final Map<JsonPersistentSampleMethod, JsonPersistentActualSample> sampleMethodJsonPersistentActualSampleMap,
                                    final SampleExecutionInformation sampleExecutionInformation,
                                    PersistentSamplerContext persistentSamplerContext) {
        final SampleDefinition sample = sampleExecutionInformation.getSampleDefinition();
        final List<MethodCall> calls = sampleExecutionInformation.getMethodCalls();

        final JsonPersistentSampleMethod persistentSampleMethod = new JsonPersistentSampleMethod(sample.getSampleId());
        final JsonPersistentActualSample jsonPersistentActualSample = new JsonPersistentActualSample();

        final Type declaredReturnType = sample.getSampledMethod().getMethod().getGenericReturnType();
        final Type[] argumentTypes = sample.getSampledMethod().getMethod().getGenericParameterTypes();

        for (final MethodCall call : calls) {
            final List<Object> argsAsPersistentBeans = convertArguments(call.getArgs(), argumentTypes, persistentSamplerContext);

            final Object returnValuePersistentBean = persistentSamplerContext.getPersistentBeanConverter().convert(call.getReturnValue(), declaredReturnType);
            final JsonPersistentParameter newParameters = new JsonPersistentParameter(argsAsPersistentBeans);

            if (!callWithSameParametersExists(jsonPersistentActualSample, newParameters)) {
                // We don't want to record redundant calls since this might lead to quite big JSON-Files. So we only add new calls.
                jsonPersistentActualSample.addCall(newParameters, returnValuePersistentBean);
            }
        }
        sampleMethodJsonPersistentActualSampleMap.put(persistentSampleMethod, jsonPersistentActualSample);
    }

    private List<Object> convertArguments(List<Object> arguments, Type[] argumentTypes, PersistentSamplerContext persistentSamplerContext) {
        List<Object> argumentPersistentBeans = new ArrayList<>();

        for (int i = 0; i < arguments.size(); i++) {
            final ParameterizedType parameterizedType = argumentTypes[i] instanceof ParameterizedType ? (ParameterizedType) argumentTypes[i] : null;
            final Object argumentPersistentBean = persistentSamplerContext.getPersistentBeanConverter().convert(arguments.get(i), parameterizedType);
            argumentPersistentBeans.add(argumentPersistentBean);
        }

        return argumentPersistentBeans;
    }

    private boolean callWithSameParametersExists(JsonPersistentActualSample jsonPersistentActualSample, JsonPersistentParameter parameter) {
        return jsonPersistentActualSample.getAllCalls().stream()//
            .anyMatch(call -> call.getPersistentParameter().equals(parameter));
    }

}
