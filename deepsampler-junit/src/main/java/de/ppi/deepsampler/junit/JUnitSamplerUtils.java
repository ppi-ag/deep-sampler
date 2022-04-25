/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.core.error.BaseException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides utilities for dealing with reflection in order to prepare Sampler in test cases using annotations and {@link SamplerFixture}s.
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

    /**
     * Loads the annotation annotationType, if it can be found. The annotation is searched in the following places:
     * <ul>
     *     <li>testMethod</li>
     *     <li>class that declares testMethod</li>
     *     <li>method {@link SamplerFixture#defineSamplers()} if testMethod is associated with a {@link SamplerFixture}</li>
     *     <li>class that implements {@link SamplerFixture} if testMethod is associated with a {@link SamplerFixture}</li>
     * </ul>
     * The annotation is searched in the sequence as shown in the list above. The first found annotation will be used.
     * A testMethod is associated with a {@link SamplerFixture}, if the method itself, or the class that declares the method,
     * is annotated with {@link UseSamplerFixture}.
     *
     * @param testMethod     a method from a test case
     * @param annotationType the searched annotation
     * @param <T>            the type of the annotation
     * @return an {@link Optional} that may contain the annotation, if it could be found.
     */
    public static <T extends Annotation> Optional<T> loadAnnotationFromTestOrSamplerFixture(final Method testMethod, final Class<T> annotationType) {
        final Optional<T> annotationFromTest = loadAnnotationFromMethodOrDeclaringClass(testMethod, annotationType);

        if (annotationFromTest.isPresent()) {
            return annotationFromTest;
        }

        return loadSamplerFixtureFromMethodOrDeclaringClass(testMethod)
                .flatMap(samplerFixture -> loadAnnotationFromMethodOrDeclaringClass(getDefineSamplersMethod(samplerFixture), annotationType));
    }

    /**
     * Loads the annotation annotationType from method, or from it's declaring class. If both places are annotated with annotationType, the one on the
     * method wins.
     *
     * @param method the method on which annotationType is searched.
     * @param annotationType the type of the requested annotation.
     * @param <T> The type of the requested annotation.
     * @return the annotation, if it could be found. Otherwise, Optional.empty() is returned.
     */
    public static <T extends Annotation> Optional<T> loadAnnotationFromMethodOrDeclaringClass(final Method method, final Class<T> annotationType) {
        final T annotationFromMethod = method.getDeclaredAnnotation(annotationType);

        if (annotationFromMethod != null) {
            return Optional.of(annotationFromMethod);
        }

        final T annotationFromClass = method.getDeclaringClass().getAnnotation(annotationType);

        if (annotationFromClass != null) {
            return Optional.of(annotationFromClass);
        }

        return Optional.empty();
    }

    /**
     * Searches recursively through all annotations of method. If an annotation can be found, that is itself annotated with metaAnnotation, metaAnnotation
     * is returned. "Recursively" means, that meta-annotations of meta-annotations will be scanned.
     * The first found metaAnnotation will be returned.
     *
     * @param method all annotations of method will be searched.
     * @param metaAnnotation the type of the meta-annotation, that is requested.
     * @param <T> the type of metaAnnotation
     * @return The meta-annotation will be returned of it could be found. Otherwise, Optional.empty() is returned.
     */
    public static <T extends Annotation> Optional<T> getMetaAnnotation(final Method method, final Class<T> metaAnnotation) {
        Optional<T> foundAnnotation;
        for (final Annotation annotation : method.getAnnotations()) {
            foundAnnotation = scanForMetaAnnotations(annotation, metaAnnotation, new ArrayList<>());

            if (foundAnnotation.isPresent()) {
                return foundAnnotation;
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static<T extends Annotation> Optional<T> scanForMetaAnnotations(final Annotation annotation, final Class<T> metaAnnotation, final List<Annotation> scannedAnnotation) {
        if (annotation.annotationType().equals(metaAnnotation)) {
            return (Optional<T>) Optional.of(annotation);
        }

        for (final Annotation parentAnnotation : annotation.annotationType().getAnnotations()) {
            if (!scannedAnnotation.contains(parentAnnotation)) {
                scannedAnnotation.add(parentAnnotation);
                return scanForMetaAnnotations(parentAnnotation, metaAnnotation, scannedAnnotation);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the {@link Method} of samplerFixture, that defines the samplers. This is just a utility, that prevents
     * the usage of a String containing the name of the method in various places in the code.
     *
     * @param samplerFixture an instance of a {@link SamplerFixture}
     * @return The {@link Method} defineSamplers. If this method cannot be found, a harsh implementation error has occurred, because
     * the method is part of the interface {@link SamplerFixture}
     */
    public static Method getDefineSamplersMethod(final SamplerFixture samplerFixture) {
        try {
            return samplerFixture.getClass().getMethod("defineSamplers");
        } catch (final NoSuchMethodException e) {
            // This would be an internal error since the requested method is part of the interface SamplerFixture.
            throw new BaseException("The SamplerFixture %s is missing a method.", e, samplerFixture.getClass().getName());
        }
    }

    /**
     * Instantiates clazz using a default constructor. If the class is an inner class, the top declaring outer class is
     * recursively searched and used to initialize the inner class.
     *
     * @param clazz the class that should be instantiated. Inner classes are allowed.
     * @param <T>   The type of the class and the returned instance.
     * @return the new instance of clazz.
     */
    public static <T> T instantiate(final Class<T> clazz) {
        try {
            if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
                final Object declaringObject = instantiate(clazz.getDeclaringClass());

                final Constructor<T> constructor = clazz.getDeclaredConstructor(clazz.getDeclaringClass());
                return constructor.newInstance(declaringObject);
            }

            final Constructor<T> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JUnitPreparationException("%s could not be instantiated.", e, clazz.getName());
        }
    }
}
