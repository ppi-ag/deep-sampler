package org.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.persistence.json.extension.DeserializationExtension;
import org.deepsampler.persistence.json.extension.SerializationExtension;
import org.deepsampler.persistence.json.model.PersistentModel;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonSourceManager implements SourceManager {

    private final JsonRecorder jsonRecorder;
    private final JsonLoader jsonLoader;

    public JsonSourceManager(Builder builder) {
        this.jsonLoader = new JsonLoader(builder.resource, builder.deserializerList, builder.moduleList);
        this.jsonRecorder = new JsonRecorder(builder.resource, builder.serializerList, builder.moduleList);
    }


    @Override
    public void record(final Map<Class<?>, ExecutionInformation> executionInformation, PersistentSamplerContext persistentSamplerContext) {
        jsonRecorder.record(executionInformation, persistentSamplerContext);
    }

    @Override
    public PersistentModel load(PersistentSamplerContext persistentSamplerContext) {
        return jsonLoader.load(persistentSamplerContext);
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
     * @param resource
     * @return
     */
    public static Builder builder(final PersistentResource resource) {
        return new Builder(resource);
    }

    public static Builder builderWithFile(String pathAsString) {
        return new Builder(new PersistentFile(Paths.get(pathAsString)));
    }

    public static class Builder {
        private final PersistentResource resource;
        private final List<SerializationExtension<?>> serializerList = new ArrayList<>();
        private final List<DeserializationExtension<?>> deserializerList = new ArrayList<>();
        private final List<Module> moduleList = new ArrayList<>();

        private Builder(PersistentResource resource) {
            this.resource = resource;
        }

        public <T> Builder addSerializer(Class<? extends T> cls, JsonSerializer<T>jsonSerializer) {
            serializerList.add(new SerializationExtension<>(cls, jsonSerializer));
            return this;
        }

        public <T> Builder addDeserializer(Class<T> cls, JsonDeserializer<? extends T> deserializer) {
            deserializerList.add(new DeserializationExtension<>(cls, deserializer));
            return this;
        }

        public <T> Builder addModule(Module module) {
            moduleList.add(module);
            return this;
        }

        public JsonSourceManager build() {
            return new JsonSourceManager(this);
        }

    }
}
