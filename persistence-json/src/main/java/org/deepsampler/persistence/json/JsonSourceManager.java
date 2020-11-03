package org.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import org.deepsampler.core.model.ExecutionInformation;
import org.deepsampler.persistence.api.SourceManager;
import org.deepsampler.persistence.json.extension.DeserializationExtension;
import org.deepsampler.persistence.json.extension.SerializationExtension;
import org.deepsampler.persistence.model.PersistentModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonSourceManager implements SourceManager {

    private final JsonRecorder jsonRecorder;
    private final JsonLoader jsonLoader;

    public JsonSourceManager(Builder builder) {
        this.jsonLoader = new JsonLoader(builder.pathToJson, builder.deserializerList, builder.moduleList);
        this.jsonRecorder = new JsonRecorder(builder.pathToJson, builder.serializerList, builder.moduleList);
    }

    @Override
    public void record(final Map<Class<?>, ExecutionInformation> executionInformation) {
        jsonRecorder.record(executionInformation);
    }

    @Override
    public PersistentModel load() {
        return jsonLoader.load();
    }

    public static Builder builder(final String pathAsString) {
        return new Builder(pathAsString);
    }

    public static Builder builder(final Path path) {
        return new Builder(path);
    }

    public static class Builder {
        private final Path pathToJson;
        private final List<SerializationExtension<?>> serializerList = new ArrayList<>();
        private final List<DeserializationExtension<?>> deserializerList = new ArrayList<>();
        private final List<Module> moduleList = new ArrayList<>();

        private Builder(Path pathToJson) {
            this.pathToJson = pathToJson;
        }

        private Builder(String pathToJsonAsString) {
            this.pathToJson = Paths.get(pathToJsonAsString);
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
