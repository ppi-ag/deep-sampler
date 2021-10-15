/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import de.ppi.deepsampler.core.error.BaseException;
import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.persistence.api.PersistentSampleManager;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;

import java.lang.reflect.*;
import java.util.Objects;
import java.util.stream.Stream;

public class JUnitPersistenceUtils {

    private JUnitPersistenceUtils() {
        // Private constructor since this utility class is not intended to be instantiated.
    }

    /**
     * If testMethod is annotated with {@link LoadSamples}, persistent samples from a JSON-File will be loaded. The filename is
     * defined by {@link LoadSamples}.
     * <p>
     * The annotation {@link UseSamplerFixture} must also be present on the method, or on the declaring class if {@link LoadSamples} is present.
     * {@link UseSamplerFixture} defines the sampler that should use the data coming from the loaded json file.
     * <p>
     * The {@link SamplerFixture}, the testMethod, or the declaring class may also be annotated with {@link UseJsonDeserializer},
     * {@link UseJsonSerializer} and {@link UseBeanConverterExtension}
     *
     * @param testMethod samples will be loaded for the method testMethod if it is annotated with {@link LoadSamples}
     */
    public static void loadSamples(final Method testMethod) {
        final LoadSamples loadSamples = testMethod.getAnnotation(LoadSamples.class);

        if (loadSamples == null) {
            return;
        }

        final JsonSourceManager jsonSourceManager = createSourceManagerWithJsonSerializerExtensions(testMethod, loadSamples);

        PersistentSampleManager sampleManager = PersistentSampler.source(jsonSourceManager);
        applyBeanExtensionsFromTestCaseAndTestFixture(testMethod, sampleManager);

        sampleManager.load();
    }


    private static JsonSourceManager createSourceManagerWithJsonSerializerExtensions(final Method testMethod, final LoadSamples loadSamples) {
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

    private static void applyBeanExtensionsFromTestCaseAndTestFixture(final Method testMethod, PersistentSampleManager persistentSampleManager) {
        applyAnnotatedBeanConverterExtension(testMethod, persistentSampleManager);

        JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(testMethod).map(JUnitPersistenceUtils::getDefineSamplersMethod)
                .ifPresent(samplerFixtureMethod -> applyAnnotatedBeanConverterExtension(samplerFixtureMethod, persistentSampleManager));
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
    private static ParameterizedType getParameterizedParentType(final Class<?> clazz) {
        if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
            return (ParameterizedType) clazz.getGenericSuperclass();
        }

        return getParameterizedParentType((Class<?>) clazz.getGenericSuperclass());
    }

    /**
     * Instantiates clazz using a default constructor. If the class is a inner class, the top declaring outer class is
     * recursively searched and used to initialize the inner class.
     *
     * @param clazz the class that should be instantiated. Inner classes are allowed.
     * @param <T>   The type of the class and the returned instance.
     * @return the new instance of clazz.
     */
    private static <T> T instantiate(final Class<T> clazz) {
        try {
            if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
                Object declaringObject = instantiate(clazz.getDeclaringClass());

                final Constructor<T> constructor = clazz.getDeclaredConstructor(clazz.getDeclaringClass());
                return constructor.newInstance(declaringObject);
            }

            final Constructor<T> constructor = clazz.getDeclaredConstructor();
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

        final JsonSourceManager jsonSourceManager = createSourceManagerWithJsonSerializerExtensions(testMethod, saveSamples);

        PersistentSampleManager sampleManager = PersistentSampler.source(jsonSourceManager);
        applyBeanExtensionsFromTestCaseAndTestFixture(testMethod, sampleManager);

        sampleManager.record();
    }

    private static JsonSourceManager createSourceManagerWithJsonSerializerExtensions(final Method testMethod, final SaveSamples saveSamples) {
        final JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);

        final String fileName = saveSamples.file().isEmpty() ? getDefaultJsonFileNameWithFolder(testMethod) : saveSamples.file();

        return persistentSampleManagerBuilder.buildWithFile(fileName);
    }


    private static String getDefaultJsonFileNameWithFolder(final Method testMethod) {
        return testMethod.getDeclaringClass().getName().replace(".", "/") + "_" + testMethod.getName() + ".json";
    }

    private static void applyAnnotatedBeanConverterExtension(final Method testMethod, final PersistentSampleManager persistentSampleManager) {
        final UseBeanConverterExtension useBeanConverterExtensionOnMethod = testMethod.getAnnotation(UseBeanConverterExtension.class);
        final UseBeanConverterExtension useBeanConverterExtensionOnClass = testMethod.getDeclaringClass().getAnnotation(UseBeanConverterExtension.class);

        Stream.of(useBeanConverterExtensionOnMethod, useBeanConverterExtensionOnClass)
                .filter(Objects::nonNull)
                .flatMap(annotation -> Stream.of(annotation.value()))
                .forEach(extensionClass -> addBeanExtension(extensionClass, persistentSampleManager));
    }

    private static void addBeanExtension(final Class<? extends BeanConverterExtension> beanConverterExtensionClass, final PersistentSampleManager persistentSampleManager) {
        BeanConverterExtension extension = instantiate(beanConverterExtensionClass);
        persistentSampleManager.addBeanExtension(extension);
    }

}
