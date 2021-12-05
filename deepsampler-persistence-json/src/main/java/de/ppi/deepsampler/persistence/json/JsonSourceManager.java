/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import de.ppi.deepsampler.core.model.ExecutionInformation;
import de.ppi.deepsampler.persistence.PersistentSamplerContext;
import de.ppi.deepsampler.persistence.api.SourceManager;
import de.ppi.deepsampler.persistence.json.extension.DeserializationExtension;
import de.ppi.deepsampler.persistence.json.extension.SerializationExtension;
import de.ppi.deepsampler.persistence.model.PersistentModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonSourceManager implements SourceManager {

    private final JsonRecorder jsonRecorder;
    private final JsonLoader jsonLoader;

    public JsonSourceManager(final Builder builder) {
        this.jsonLoader = new JsonLoader(builder.resource, builder.deserializerList, builder.moduleList);
        this.jsonRecorder = new JsonRecorder(builder.resource, builder.serializerList, builder.moduleList);
    }


    @Override
    public void save(final Map<Class<?>, ExecutionInformation> executionInformation, final PersistentSamplerContext persistentSamplerContext) {
        jsonRecorder.recordExecutionInformation(executionInformation, persistentSamplerContext);
    }

    @Override
    public PersistentModel load() {
        return jsonLoader.load();
    }

    /**
     * Create a builder for the {@link JsonSourceManager} with an {@link PersistentResource}. A persistent
     * resource is an abstract representation of an persistent file. There are two different ways
     * you can access files in java:
     * <ul>
     *     <li>via classpath in a jar</li>
     *     <li>in the filesystem</li>
     * </ul>
     *
     * For each way there is an implementation of {@link PersistentResource}:
     * <ul>
     *     <li>{@link PersistentFile}</li>
     *     <li>{@link PersistentClassPathResource}</li>
     * </ul>
     *
     * @return The Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PersistentResource resource;
        private final List<SerializationExtension<?>> serializerList = new ArrayList<>();
        private final List<DeserializationExtension<?>> deserializerList = new ArrayList<>();
        private final List<Module> moduleList = new ArrayList<>();

        public <T> Builder addSerializer(final Class<T> typeToSerialize, final JsonSerializer<T>jsonSerializer) {
            serializerList.add(new SerializationExtension<>(typeToSerialize, jsonSerializer));
            return this;
        }

        public <T> Builder addDeserializer(final Class<T> typeToSerialize, final JsonDeserializer<T> deserializer) {
            deserializerList.add(new DeserializationExtension<>(typeToSerialize, deserializer));
            return this;
        }

        public Builder addModule(final Module module) {
            moduleList.add(module);
            return this;
        }

        public JsonSourceManager buildWithResource(final PersistentResource resource) {
            this.resource = resource;
            return new JsonSourceManager(this);
        }

        public JsonSourceManager buildWithFile(final String filePath) {
            return buildWithFile(Paths.get(filePath));
        }

        public JsonSourceManager buildWithFile(final Path filePath) {
            this.resource = new PersistentFile(filePath);
            return new JsonSourceManager(this);
        }

        public JsonSourceManager buildWithClassPathResource(final String filePath, final Class<?> anchor) {
            this.resource = new PersistentClassPathResource(filePath, anchor);
            return new JsonSourceManager(this);
        }

    }
}
