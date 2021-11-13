/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.api.Sampler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides utilities for dealing with reflection in order to prepare Sampler in test cases using the Annotation {@link PrepareSampler}
 * and {@link UseSamplerFixture}.
 */
public class JUnitSamplerUtils {

    private JUnitSamplerUtils() {
        // Private constructor since this utility class is not intended to be instantiated.
    }

    /**
     * Searches for fields in targetObject that are annotated with {@link PrepareSampler} and injects Sampler instances in all these fields.
     *
     * @param targetObject the object in which Samplers should be injected.
     */
    public static void injectSamplers(final Object targetObject) {
        getDeclaredAndInheritedFields(targetObject.getClass())//
                .filter(JUnitSamplerUtils::shouldBeSampled)//
                .forEach(field -> assignNewSamplerToField(targetObject, field));
    }


    /**
     * If testMethod, or the class that declares testMethod, is annotated with @{@link UseSamplerFixture}, the {@link SamplerFixture}
     * is instantiated and returned, but not executed.
     * <p>
     * If both, the method and it's declaring class are annotated with @{@link UseSamplerFixture} only the one from the method is
     * instantiated. So the {@link SamplerFixture}s on methods override {@link SamplerFixture}s on classes.
     *
     * @param testMethod the corresponding {@link SamplerFixture} will be instantiated, but not executed.
     * @return the instantiated {@link SamplerFixture} that will be used for testMethod.
     */
    public static Optional<SamplerFixture> loadSamplerFixtureFromMethodOrDeclaringClass(final Method testMethod) {
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
     * If testMethod, or the class that declares testMethod, is annotated with @{@link UseSamplerFixture}, the {@link SamplerFixture}
     * is instantiated and executed before testMethod executes. Samples, that have been defined by the {@link SamplerFixture} will be active
     * during the execution of testMethod.
     * <p>
     * If both, the method and it's declaring class are annotated with @{@link UseSamplerFixture} only the one from the method is
     * used. So the {@link SamplerFixture}s on methods override {@link SamplerFixture}s on classes.
     *
     * @param testMethod the test-method that should be initialized with a {@link SamplerFixture}
     */
    public static void applySamplesFromSamplerFixture(final Method testMethod) {
        loadSamplerFixtureFromMethodOrDeclaringClass(testMethod).ifPresent(samplerFixture -> {
            injectSamplers(samplerFixture);
            samplerFixture.defineSamplers();
        });
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
     * Prepares a new Sampler corresponding to the type of field and assigns the Sampler the field on testInstance.
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
