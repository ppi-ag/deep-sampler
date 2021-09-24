/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.core.error.BaseException;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides utilities for dealing with reflection in order to prepare Sampler in Testcases using the Annotation {@link PrepareSampler}.
 */
public class JUnitPluginUtils {

    private JUnitPluginUtils() {
        // Private constructor since this utility class is not intended to be instantiated.
    }

    public static void injectSamplers(final Object targetObject) {
        JUnitPluginUtils.getDeclaredAndInheritedFields(targetObject.getClass())//
                .filter(JUnitPluginUtils::shouldBeSampled)//
                .forEach(field -> JUnitPluginUtils.assignNewSamplerToField(targetObject, field));
    }

    private static Optional<SamplerFixture> loadSamplerFixture(final Method testMethod) {
        final UseSamplerFixture fixtureOnMethod = testMethod.getAnnotation(UseSamplerFixture.class);
        final UseSamplerFixture fixtureOnClass = testMethod.getDeclaringClass().getAnnotation(UseSamplerFixture.class);

        if (fixtureOnMethod == null && fixtureOnClass == null) {
            return Optional.empty();
        }


        final Class<? extends SamplerFixture> samplerFixtureClass;

        if (fixtureOnMethod != null) {
            samplerFixtureClass = fixtureOnMethod.value();
        } else {
            samplerFixtureClass = fixtureOnClass.value();
        }

        try {
            final Constructor<? extends SamplerFixture> samplerFixtureClassConstructor = samplerFixtureClass.getConstructor();
            return Optional.of(samplerFixtureClassConstructor.newInstance());
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JUnitPreparationException("The SamplerFixture %s could not be loaded", e, samplerFixtureClass.getName());
        } catch (final NoSuchMethodException e) {
            throw new JUnitPreparationException("The SamplerFixture %s must provide a default constructor.", e, samplerFixtureClass.getName());
        }
    }

    /**
     * The annotation {@link UseSamplerFixture} references a {@link SamplerFixture} that can be used to define Samplers in a
     * reusable manner. A test can use a {@link SamplerFixture} if the testMethod or the class that declares testMethod is annotated
     * with {@link UseSamplerFixture}. The annotation at method-level overrides the annotation at class-level.
     *
     * @param testMethod the test-method that should be initialized with a {@link SamplerFixture}
     */
    public static void applySamplesFromSamplerFixture(final Method testMethod) {
        loadSamplerFixture(testMethod).ifPresent(samplerFixture -> {
            injectSamplers(samplerFixture);
            samplerFixture.defineSamplers();
        });
    }

    public static void loadSamples(final Method testMethod) {
        final LoadSamples loadSamples = testMethod.getAnnotation(LoadSamples.class);

        if (loadSamples == null) {
            return;
        }

        final JsonSourceManager jsonSourceManager = createSourceManager(testMethod, loadSamples);

        PersistentSampler.source(jsonSourceManager).load();
    }


    private static JsonSourceManager createSourceManager(final Method testMethod, final LoadSamples loadSamples) {
        JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);

        if (!loadSamples.classPath().isEmpty()) {
            return persistentSampleManagerBuilder.buildWithClassPathResource(loadSamples.classPath(), testMethod.getDeclaringClass());
        } else if (!loadSamples.file().isEmpty()) {
            return persistentSampleManagerBuilder.buildWithFile(loadSamples.file());
        } else {
            return persistentSampleManagerBuilder.buildWithClassPathResource(getDefaultJsonFileName(testMethod), testMethod.getDeclaringClass());
        }
    }

    private static void applyJsonSerializersFromTestCaseAndTestFixture(Method testMethod, JsonSourceManager.Builder persistentSampleManagerBuilder) {
        applyAnnotatedJsonSerializers(testMethod, persistentSampleManagerBuilder);
        applyAnnotatedJsonDeserializers(testMethod, persistentSampleManagerBuilder);

        loadSamplerFixture(testMethod).map(JUnitPluginUtils::getDefineSamplersMethod)
                .ifPresent(samplerFixtureMethod -> {
                    applyAnnotatedJsonSerializers(samplerFixtureMethod, persistentSampleManagerBuilder);
                    applyAnnotatedJsonDeserializers(samplerFixtureMethod, persistentSampleManagerBuilder);
                });
    }

    private static Method getDefineSamplersMethod(SamplerFixture samplerFixture) {
        try {
            return samplerFixture.getClass().getMethod("defineSamplers");
        } catch (NoSuchMethodException e) {
            throw new BaseException("The SamplerFixture %s is missing a method.", e, samplerFixture.getClass().getName());
        }
    }

    private static void applyAnnotatedJsonSerializers(Method testMethod, JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final UseJsonSerializer[] serializersOnMethod = testMethod.getAnnotationsByType(UseJsonSerializer.class);
        final UseJsonSerializer[] serializersOnClass = testMethod.getDeclaringClass().getAnnotationsByType(UseJsonSerializer.class);

        Stream.of(serializersOnClass, serializersOnMethod)
                .flatMap(Stream::of)
                .forEach(serializerAnnotation -> addSerializer(serializerAnnotation, persistentSampleManagerBuilder));
    }

    private static void applyAnnotatedJsonDeserializers(Method testMethod, JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final UseJsonDeserializer[] deserializersOnMethod = testMethod.getAnnotationsByType(UseJsonDeserializer.class);
        final UseJsonDeserializer[] deserializersOnClass = testMethod.getDeclaringClass().getAnnotationsByType(UseJsonDeserializer.class);

        Stream.of(deserializersOnMethod, deserializersOnClass)
                .flatMap(Stream::of)
                .forEach(deserializerAnnotation -> addDeserializer(deserializerAnnotation, persistentSampleManagerBuilder));
    }

    private static void addSerializer(UseJsonSerializer useJsonSerializer, JsonSourceManager.Builder persistentSampleManagerBuilder) {
        Class<? extends JsonSerializer<?>> serializerClass = useJsonSerializer.serializer();

        JsonSerializer jsonSerializer = instantiate(serializerClass);

        persistentSampleManagerBuilder.addSerializer(useJsonSerializer.forType(), jsonSerializer);
    }

    private static <T> T instantiate(Class<T> clazz) {
        if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            throw new JUnitPreparationException("%s is an inner class, but it is not declared as static.", clazz.getName());
        }

        try {
            Constructor<T> ctor = clazz.getConstructor();
            return ctor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JUnitPreparationException("%s could not be instantiated.", e, clazz.getName());
        }
    }

    private static void addDeserializer(UseJsonDeserializer useJsonDeserializer, JsonSourceManager.Builder persistentSampleManagerBuilder) {
        Class<? extends JsonDeserializer<?>> deserializerClass = useJsonDeserializer.deserializer();

        JsonDeserializer jsonDeserializer = instantiate(deserializerClass);

        persistentSampleManagerBuilder.addDeserializer(useJsonDeserializer.forType(), jsonDeserializer);
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
        JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);

        final String fileName = saveSamples.file().isEmpty() ? getDefaultJsonFileNameWithFolder(testMethod) : saveSamples.file();

        return persistentSampleManagerBuilder.buildWithFile(fileName);
    }


    private static String getDefaultJsonFileNameWithFolder(final Method testMethod) {
        return testMethod.getDeclaringClass().getName().replace(".", "/") + "_" + testMethod.getName() + ".json";
    }

    private static String getDefaultJsonFileName(final Method testMethod) {
        return testMethod.getDeclaringClass().getSimpleName() + "_" + testMethod.getName() + ".json";
    }

    /**
     * Finds all properties of clazz including inherited properties.
     *
     * @param clazz The clazz of which the properties are needed.
     * @return A Stream containing the found Fields.
     */
    public static Stream<Field> getDeclaredAndInheritedFields(final Class<?> clazz) {
        final Stream<Field> declaredFields = Arrays.stream(clazz.getDeclaredFields());

        if (!Object.class.equals(clazz.getSuperclass())) {
            return Stream.concat(declaredFields, getDeclaredAndInheritedFields(clazz.getSuperclass()));
        }

        return declaredFields;
    }

    /**
     * Checks whether a Field is annotated with {@link PrepareSampler} or not.
     *
     * @param field The field that is suspected to be annotated with {@link PrepareSampler}.
     * @return <code>true</code> if field is annotated with {@link PrepareSampler}
     */
    public static boolean shouldBeSampled(final Field field) {
        return field.getAnnotation(PrepareSampler.class) != null;
    }

    /**
     * Prepares a new Sampler according to the type of field and assigns the Sampler the field on testInstance.
     *
     * @param testInstance the object in which the sampler should be injected.
     * @param field        the field that should be populated with a new Sampler.
     */
    @SuppressWarnings("java:S3011") // Ignore warnings regarding field access via reflection
    public static void assignNewSamplerToField(final Object testInstance, final Field field) {
        final Object sampler = Sampler.prepare(field.getType());
        try {
            field.setAccessible(true);
            field.set(testInstance, sampler);
        } catch (final IllegalAccessException e) {
            throw new JUnitPreparationException("No access to property %s#%s", e, testInstance.getClass().getName(), field.getName());
        }
    }
}
