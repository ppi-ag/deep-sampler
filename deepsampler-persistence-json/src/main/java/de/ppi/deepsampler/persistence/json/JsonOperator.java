/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.ppi.deepsampler.persistence.json.extension.PlainByteArrayDeserializer;
import de.ppi.deepsampler.persistence.json.extension.PlainByteArraySerializer;
import de.ppi.deepsampler.persistence.json.extension.DeserializationExtension;
import de.ppi.deepsampler.persistence.json.extension.SerializationExtension;

import java.util.List;

public abstract class JsonOperator {

    private final PersistentResource persistentResource;
    private final List<SerializationExtension<?>> serializationExtensions;
    private final List<DeserializationExtension<?>> deserializationExtensions;
    private final List<Module> moduleList;

    protected JsonOperator(final PersistentResource persistentResource,
                           final List<DeserializationExtension<?>> deserializerList,
                           final List<SerializationExtension<?>> serializerList,
                           final List<Module> moduleList) {
        this.persistentResource = persistentResource;
        this.serializationExtensions = serializerList;
        this.deserializationExtensions = deserializerList;
        this.moduleList = moduleList;
    }

    protected PersistentResource getPersistentResource() {
        return persistentResource;
    }

    protected ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        
        objectMapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new DeepSamplerSpecificModule());
        
        objectMapper.setDefaultTyping(new CustomTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, objectMapper.getPolymorphicTypeValidator()));

        applyCustomExtensions(objectMapper);

        return objectMapper;
    }

    @SuppressWarnings("unchecked")
    private void applyCustomExtensions(final ObjectMapper objectMapper) {
        final SimpleModule simpleModule = new SimpleModule();

        for (final DeserializationExtension<?> deserializationExtension : deserializationExtensions) {
            final DeserializationExtension<Object> deserializationObjExtension = (DeserializationExtension<Object>) deserializationExtension;
            simpleModule.addDeserializer(deserializationObjExtension.getTypeToSerialize(), deserializationObjExtension.getJsonDeserializer());
        }

        for (final SerializationExtension<?> serializationExtension : serializationExtensions) {
            final SerializationExtension<Object> serializationObjExtension = (SerializationExtension<Object>) serializationExtension;
            simpleModule.addSerializer(serializationObjExtension.getTypeToSerialize(), serializationObjExtension.getJsonSerializer());
        }
        objectMapper.registerModule(simpleModule);

        for (final Module module : moduleList) {
            objectMapper.registerModule(module);
        }
    }

    private static class CustomTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {
        private static final String TYPE = "@type";

        public CustomTypeResolverBuilder(final ObjectMapper.DefaultTyping t, final PolymorphicTypeValidator ptv) {
            super(t, ptv);

            init(JsonTypeInfo.Id.CLASS, null);
            inclusion(JsonTypeInfo.As.PROPERTY);
            typeProperty(TYPE);
        }

        @Override
        public boolean useForType(final JavaType t) {
            return !t.isContainerType() && super.useForType(t);
        }
    }
}
