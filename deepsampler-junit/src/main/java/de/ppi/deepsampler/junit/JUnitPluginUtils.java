/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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

    public static void applyTestFixture(final Method testMethod) {
        final UseSamplerFixture fixture = testMethod.getAnnotation(UseSamplerFixture.class);

        if (fixture == null) {
            return;
        }

        final Class<? extends SamplerFixture> samplerFixtureClass = fixture.value();

        try {
            final Constructor<? extends SamplerFixture> samplerFixtureClassConstructor = samplerFixtureClass.getConstructor();
            final SamplerFixture samplerFixture = samplerFixtureClassConstructor.newInstance();

            injectSamplers(samplerFixture);
            samplerFixture.defineSamplers();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JUnitPreparationException("The SamplerFixture %s could not be loaded", e, samplerFixtureClass.getName());
        } catch (final NoSuchMethodException e) {
            throw new JUnitPreparationException("The SamplerFixture %s must provide a default constructor.", e, samplerFixtureClass.getName());
        }
    }

    public static void loadSamples(final Method testMethod) {
        final LoadSamples loadSamples = testMethod.getAnnotation(LoadSamples.class);

        if (loadSamples == null) {
            return;
        }

        final JsonSourceManager.Builder persistentSampleManagerBuilder = loadBuilder(loadSamples.persistenceManagerProvider());

        final JsonSourceManager jsonSourceManager = createSourceManager(testMethod, loadSamples, persistentSampleManagerBuilder);

        PersistentSampler.source(jsonSourceManager).load();
    }

    private static JsonSourceManager.Builder loadBuilder(final Class<? extends PersistentSampleManagerProvider> persistenceManagerProviderClass) {
        try {
            final Constructor<? extends PersistentSampleManagerProvider> persistenceManagerProviderClassConstructor = persistenceManagerProviderClass.getConstructor();
            final PersistentSampleManagerProvider persistentSampleManagerProvider = persistenceManagerProviderClassConstructor.newInstance();

            return persistentSampleManagerProvider.configurePersistentSampleManager();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JUnitPreparationException("The PersistentSampleManagerProvider %s could not be loaded.", e, persistenceManagerProviderClass.getName());
        } catch (final NoSuchMethodException e) {
            throw new JUnitPreparationException("%s must define a default constructor.", e, persistenceManagerProviderClass.getName());
        }
    }

    private static JsonSourceManager createSourceManager(final Method testMethod, final LoadSamples loadSamples, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        if (!loadSamples.classPath().isEmpty()) {
            return persistentSampleManagerBuilder.buildWithClassPathResource(loadSamples.classPath(), testMethod.getDeclaringClass());
        } else if (!loadSamples.file().isEmpty()) {
            return persistentSampleManagerBuilder.buildWithFile(loadSamples.file());
        } else {
            return persistentSampleManagerBuilder.buildWithClassPathResource(getDefaultJsonFileName(testMethod), testMethod.getDeclaringClass());
        }
    }

    public static void saveSamples(final Method testMethod) {
        final SaveSamples loadSamples = testMethod.getAnnotation(SaveSamples.class);

        if (loadSamples == null) {
            return;
        }

        final JsonSourceManager.Builder persistentSampleManagerBuilder = loadBuilder(loadSamples.persistenceManagerProvider());

        final String fileName = loadSamples.file().isEmpty() ? getDefaultJsonFileNameWithFolder(testMethod) : loadSamples.file();

        PersistentSampler.source(persistentSampleManagerBuilder.buildWithFile(fileName)).record();
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
