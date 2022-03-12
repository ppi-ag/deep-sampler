/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
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

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonSourceManager implements SourceManager {

    private final JsonRecorder jsonRecorder;
    private final JsonLoader jsonLoader;

    public JsonSourceManager(final Builder builder) {
        this.jsonLoader = new JsonLoader(builder.resource, builder.deserializerList, builder.moduleList, builder.charset);
        this.jsonRecorder = new JsonRecorder(builder.resource, builder.serializerList, builder.moduleList, builder.charset);
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
     * Creates a builder for the {@link JsonSourceManager} with an {@link PersistentResource}. A persistent
     * resource is an abstract representation of a persistent file. There are two different ways
     * you can access files in java:
     * <ul>
     *     <li>via classpath in a jar</li>
     *     <li>in the filesystem</li>
     * </ul>
     *
     * There is an implementation of {@link PersistentResource} for both ways:
     * <ul>
     *     <li>{@link PersistentFile}</li>
     *     <li>{@link PersistentClassPathResource}</li>
     * </ul>
     *
     * Default charset is determined by {@link Charset#defaultCharset()}.
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
        private Charset charset = Charset.defaultCharset();

        /**
         * The underlying persistence-api is by default Jackson. This api is capable to serialize various types
         * by default. However, sometimes objects cannot be serialized by Jackson by default. In these cases, custom
         * {@link JsonSerializer}s must be implemented. This method can be used to register such a {@link JsonSerializer}.
         *
         * @param typeToSerialize a class representing the type, which will be serialized by jsonSerializer.
         * @param jsonSerializer the custom {@link JsonSerializer}, that is able to serialize objects of the type typeToSerialize.
         * @param <T> The type, which will be serialized by jsonSerializer.
         * @return This {@link Builder} for a fluent-api.
         */
        public <T> Builder addSerializer(final Class<T> typeToSerialize, final JsonSerializer<T>jsonSerializer) {
            serializerList.add(new SerializationExtension<>(typeToSerialize, jsonSerializer));
            return this;
        }

        /**
         * The underlying persistence-api is by default Jackson. This api is capable to deserialize various types
         * by default. However, sometimes objects cannot be deserialized by Jackson by default. In these cases, custom
         * {@link JsonDeserializer}s must be implemented. This method can be used to register such a {@link JsonDeserializer}.
         *
         * @param typeToDeserialize a class representing the type, which will be deserialized by jsonDeserializer.
         * @param jsonDeserializer the custom {@link JsonDeserializer}, that is able to deserialize objects of the type typeToDeserialize.
         * @param <T> The type, which will be deserialized by jsonDeserializer.
         * @return This {@link Builder} for a fluent-api.
         */
        public <T> Builder addDeserializer(final Class<T> typeToDeserialize, final JsonDeserializer<T> jsonDeserializer) {
            deserializerList.add(new DeserializationExtension<>(typeToDeserialize, jsonDeserializer));
            return this;
        }

        /**
         * The underlying persistence-api is by default Jackson. This api is capable to serialize and deserialize various types
         * of objects by default. However, sometimes objects cannot be serialized or deserialized by Jackson by default.
         * Jackson has the possibility to define {@link Module}s which bind various custom {@link JsonSerializer}s and
         * {@link JsonDeserializer}s. These modules can be registered with Jackson using this method.
         * @param module The Jackson {@link Module}
         * @return This {@link Builder} for a fluent-api.
         */
        public Builder addModule(final Module module) {
            moduleList.add(module);
            return this;
        }

        /**
         * Changes the charset to charset. The default charset is defined by {@link Charset#defaultCharset()}.
         * @param charset The charset that will be used by this {@link SourceManager}.
         * @return This {@link Builder} for a fluent-api.
         */
        public Builder withCharset(final Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * A {@link JsonSourceManager} is able to read and write resources which are abstracted by the interface
         * {@link PersistentResource}. This allows to write and read JSON not only from files but from various
         * other resources, like JSON-capable object-oriented DBs.
         *
         * @param resource a custom {@link PersistentResource} which will be used to write and read the JSON.
         * @return This {@link Builder} for a fluent-api.
         */
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
