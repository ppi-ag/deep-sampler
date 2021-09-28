/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import de.ppi.deepsampler.core.error.BaseException;
import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;

import java.lang.reflect.*;
import java.util.stream.Stream;

public class JUnitPersistenceUtils {

    private JUnitPersistenceUtils() {
        // Private constructor since this utility class is not intended to be instantiated.
    }

    /**
     * If testMethod is annotated with {@link LoadSamples} persistent samples from a JSON-File will be loaded. The file is
     * defined by the {@link LoadSamples}.
     *
     * @param testMethod samples will be laoded for the method testMethod if it is annotated with {@link LoadSamples}
     */
    public static void loadSamples(final Method testMethod) {
        final LoadSamples loadSamples = testMethod.getAnnotation(LoadSamples.class);

        if (loadSamples == null) {
            return;
        }

        final JsonSourceManager jsonSourceManager = createSourceManager(testMethod, loadSamples);

        PersistentSampler.source(jsonSourceManager).load();
    }


    private static JsonSourceManager createSourceManager(final Method testMethod, final LoadSamples loadSamples) {
        final JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);

        if (!loadSamples.classPath().isEmpty()) {
            return persistentSampleManagerBuilder.buildWithClassPathResource(loadSamples.classPath(), testMethod.getDeclaringClass());
        } else if (!loadSamples.file().isEmpty()) {
            return persistentSampleManagerBuilder.buildWithFile(loadSamples.file());
        } else {
            return persistentSampleManagerBuilder.buildWithClassPathResource(getDefaultJsonFileName(testMethod), testMethod.getDeclaringClass());
        }
    }

    private static String getDefaultJsonFileName(final Method testMethod) {
        return testMethod.getDeclaringClass().getSimpleName() + "_" + testMethod.getName() + ".json";
    }


    private static void applyJsonSerializersFromTestCaseAndTestFixture(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        applyAnnotatedJsonSerializers(testMethod, persistentSampleManagerBuilder);
        applyAnnotatedJsonDeserializers(testMethod, persistentSampleManagerBuilder);

        JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(testMethod).map(JUnitPersistenceUtils::getDefineSamplersMethod)
                .ifPresent(samplerFixtureMethod -> {
                    applyAnnotatedJsonSerializers(samplerFixtureMethod, persistentSampleManagerBuilder);
                    applyAnnotatedJsonDeserializers(samplerFixtureMethod, persistentSampleManagerBuilder);
                });
    }

    private static Method getDefineSamplersMethod(final SamplerFixture samplerFixture) {
        try {
            return samplerFixture.getClass().getMethod("defineSamplers");
        } catch (final NoSuchMethodException e) {
            // This would be an internal error since the requested method is part of the interface SamplerFixture.
            throw new BaseException("The SamplerFixture %s is missing a method.", e, samplerFixture.getClass().getName());
        }
    }

    private static void applyAnnotatedJsonSerializers(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final UseJsonSerializer[] serializersOnMethod = testMethod.getAnnotationsByType(UseJsonSerializer.class);
        final UseJsonSerializer[] serializersOnClass = testMethod.getDeclaringClass().getAnnotationsByType(UseJsonSerializer.class);

        Stream.of(serializersOnClass, serializersOnMethod)
                .flatMap(Stream::of)
                .forEach(serializerAnnotation -> addSerializer(serializerAnnotation, persistentSampleManagerBuilder));
    }

    private static void applyAnnotatedJsonDeserializers(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final UseJsonDeserializer[] deserializersOnMethod = testMethod.getAnnotationsByType(UseJsonDeserializer.class);
        final UseJsonDeserializer[] deserializersOnClass = testMethod.getDeclaringClass().getAnnotationsByType(UseJsonDeserializer.class);

        Stream.of(deserializersOnMethod, deserializersOnClass)
                .flatMap(Stream::of)
                .forEach(deserializerAnnotation -> addDeserializer(deserializerAnnotation, persistentSampleManagerBuilder));
    }

    @SuppressWarnings({"java:S3740", "unchecked", "rawtypes"})
    // The raw use of parameterized class JsonSerializer is unavoidable here, because the serializerClass is coming
    // from an annotation where no generics other then wildcards are allowed. Since addDeserialzer() expects that serializerClass and typeToSerialize have
    // the same type T, we cannot use any generics here - unfortunately.
    private static void addSerializer(final UseJsonSerializer useJsonSerializer, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final Class<? extends JsonSerializer<?>> serializerClass = useJsonSerializer.serializer();
        final Class<?> typeToSerialize = useJsonSerializer.forType();

        validateTypesOfSerializerAndSerializable(serializerClass, typeToSerialize);

        final JsonSerializer jsonSerializer = instantiate(serializerClass);

        persistentSampleManagerBuilder.addSerializer(typeToSerialize, jsonSerializer);
    }

    /**
     * Checks if the generic type of the serialzer is the same as the type of the class that should be serialized by the serialzer. Usually this would be
     * ensured by generics at compile time, but annotations don't allow the useage of generics other then wildcards.
     *
     * @param serializerClass the type of the {@link JsonSerializer}
     * @param typeToSerialize the type of the classes that should be serialized by serializerClass
     */
    private static void validateTypesOfSerializerAndSerializable(final Class<? extends JsonSerializer<?>> serializerClass, final Class<?> typeToSerialize) {
        final Type[] typeArguments = getParameterizedParentType(serializerClass).getActualTypeArguments();

        if (!typeArguments[0].equals(typeToSerialize)) {
            throw new InvalidConfigException("%s must have a parameter type of type %s since the serializer is registered for the latter type. But it is %s",
                    serializerClass.getName(),
                    typeToSerialize.getName(),
                    typeArguments[0].getTypeName());
        }
    }

    @SuppressWarnings({"java:S3740", "unchecked", "rawtypes"})
    // The raw use of parameterized class JsonSerializer is unavoidable here, because the serializerClass is coming
    // from an annotation where no generics other then wildcards are allowed. Since addDeserialzer() expects that serializerClass and typeToSerialize have
    // the same type T, we cannot use any generics here - unfortunately.
    private static void addDeserializer(final UseJsonDeserializer useJsonDeserializer, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final Class<? extends JsonDeserializer<?>> deserializerClass = useJsonDeserializer.deserializer();
        final Class<?> typeToDeserialize = useJsonDeserializer.forType();

        validateTypesOfDeserializerAndDeserializable(deserializerClass, typeToDeserialize);

        final JsonDeserializer jsonDeserializer = instantiate(deserializerClass);

        persistentSampleManagerBuilder.addDeserializer(typeToDeserialize, jsonDeserializer);
    }

    /**
     * Checks if the generic type of the serialzer is the same as the type of the class that should be serialized by the serialzer. Usually this would be
     * ensured by generics at compile time, but annotations don't allow the useage of generics other then wildcards.
     *
     * @param deserializerClass the type of the {@link JsonDeserializer}
     * @param typeToDeserialize the type of the classes that should be deserialized by deserializerClass
     */
    private static void validateTypesOfDeserializerAndDeserializable(final Class<? extends JsonDeserializer<?>> deserializerClass, final Class<?> typeToDeserialize) {
        final Type[] typeArguments = getParameterizedParentType(deserializerClass).getActualTypeArguments();

        if (!typeArguments[0].equals(typeToDeserialize)) {
            throw new InvalidConfigException("%s must have a parameter type of type %s since the deserializer is registered for the latter type. But it is %s",
                    deserializerClass.getName(),
                    typeToDeserialize.getName(),
                    typeArguments[0].getTypeName());
        }
    }

    /**
     * If clazz is a subclass that does not declare a generic type parameter on its own and relies on the type parameter of a
     * parent class, we have to search the type hierarchy for the first parameterized type.
     *
     * @param clazz the class for which we want to find the first parameterized superclass.
     * @return the first parameterized superclass
     */
    private static  ParameterizedType getParameterizedParentType(final Class<?> clazz) {
        if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
            return (ParameterizedType) clazz.getGenericSuperclass();
        }

        return getParameterizedParentType((Class<?>) clazz.getGenericSuperclass());
    }

    private static <T> T instantiate(final Class<T> clazz) {
        try {
            if (clazz.isMemberClass()) {
                Object declaringObject = instantiate(clazz.getDeclaringClass());

                final Constructor<T> constructor = clazz.getDeclaredConstructor(clazz.getDeclaringClass());
                return constructor.newInstance(declaringObject);
            }

            final Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JUnitPreparationException("%s could not be instantiated.", e, clazz.getName());
        }
    }

    public static void saveSamples(final Method testMethod) {
        final SaveSamples saveSamples = testMethod.getAnnotation(SaveSamples.class);

        if (saveSamples == null) {
            return;
        }

        final JsonSourceManager sourceManager = createSourceManager(testMethod, saveSamples);
        PersistentSampler.source(sourceManager).record();
    }

    private static JsonSourceManager createSourceManager(final Method testMethod, final SaveSamples saveSamples) {
        final JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);

        final String fileName = saveSamples.file().isEmpty() ? getDefaultJsonFileNameWithFolder(testMethod) : saveSamples.file();

        return persistentSampleManagerBuilder.buildWithFile(fileName);
    }


    private static String getDefaultJsonFileNameWithFolder(final Method testMethod) {
        return testMethod.getDeclaringClass().getName().replace(".", "/") + "_" + testMethod.getName() + ".json";
    }

}
