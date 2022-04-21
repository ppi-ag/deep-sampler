/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.api.PersistentSampleManager;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.api.SourceManager;
import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A collection of tools to load and save samples from various sources like Json-files. The configuration is done using
 * annotations on test methods or the classes that declare the methods.
 */
public class JUnitPersistenceUtils {


    private JUnitPersistenceUtils() {
        // Private constructor since this utility class is not intended to be instantiated.
    }

    /**
     * Tests-methods, that are annotated with any annotation that is itself annotated with {@link UseSourceManagerForLoading},
     * run in player mode. If a method is annotated with this meta-annotation, samples are loaded before the method is executed.
     * <p>
     * The method must also be annotated with @{@link UseSamplerFixture}.
     * This annotation binds a {@link SamplerFixture} to the test, which declares the sampler that define which methods are stubbed.
     * These stubs are also the stubs, that consume the samples from the {@link SourceManager} i.e. Json.
     * <p>
     * The samples can be recorded using any annotation that itself is annotated with {@link UseSourceManagerForSaving}. See
     * also {@link JUnitPersistenceUtils#saveSamples(Method)}.
     * <p>
     *
     * @param testMethod samples will be loaded for the method testMethod, if it is annotated with the meta-annotation {@link UseSourceManagerForLoading}.
     *                   This means, the method must be annotated with any annotation, that itself is annotated with {@link UseSourceManagerForLoading}
     */
    public static void loadSamples(final Method testMethod) {
        final Optional<UseSourceManagerForLoading> sourceManagerForLoading = JUnitSamplerUtils.getMetaAnnotation(testMethod, UseSourceManagerForLoading.class);

        if (!sourceManagerForLoading.isPresent()) {
            return;
        }

        final SourceManagerFactory<? extends SourceManager> sourceManagerFactory = JUnitSamplerUtils.instantiate(sourceManagerForLoading.get().value());

        final SourceManager sourceManager = sourceManagerFactory.createSourceManagerToLoadSamples(testMethod);
        final PersistentSampleManager sampleManager = PersistentSampler.source(sourceManager);

        applyBeanExtensionsFromTestCaseAndTestFixture(testMethod, sampleManager);

        sampleManager.load();
    }

    /**
     * Tests-methods, that are annotated with any annotation that is itself annotated with {@link UseSourceManagerForSaving},
     * run in recorder mode. This means, that stubs will call the original methods and record the parameter values
     * and return values of the original method. The recorded sample is saved using the {@link SourceManager}, by default this
     * will ba a JSON-file.
     * <p>
     * The recorded samples can be replayed by annotating the test-method with any annotation that itself is annotated with
     * {@link UseSourceManagerForLoading}. See also {@link JUnitPersistenceUtils#loadSamples(Method)}.
     * <p>
     * The annotation {@link UseSamplerFixture} must also be present on the method, or on the declaring class.
     * {@link UseSamplerFixture} binds a {@link SamplerFixture} to the test.
     * <p>
     *
     * @param testMethod samples will be recorded for the testMethod, if it is annotated with the meta-annotation {@link UseSourceManagerForSaving}.
     *                   This means, the method must be annotated with any annotation, that itself is annotated with {@link UseSourceManagerForSaving}
     */
    public static void saveSamples(final Method testMethod) {
        final Optional<UseSourceManagerForSaving> sourceManagerForSaving = JUnitSamplerUtils.getMetaAnnotation(testMethod, UseSourceManagerForSaving.class);

        if (!sourceManagerForSaving.isPresent()) {
            return;
        }

        final SourceManagerFactory<? extends SourceManager> sourceManagerFactory = JUnitSamplerUtils.instantiate(sourceManagerForSaving.get().value());

        final SourceManager sourceManager = sourceManagerFactory.createSourceManagerToSaveSamples(testMethod);
        final PersistentSampleManager sampleManager = PersistentSampler.source(sourceManager);

        applyBeanExtensionsFromTestCaseAndTestFixture(testMethod, sampleManager);

        sampleManager.recordSamples();
    }


    private static void applyBeanExtensionsFromTestCaseAndTestFixture(final Method testMethod, final PersistentSampleManager persistentSampleManager) {
        // 1. Apply BeanConverter from TestFixture...
        JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(testMethod)
                .map(JUnitSamplerUtils::getDefineSamplersMethod)
                .ifPresent(samplerFixtureMethod -> applyAnnotatedBeanConverterExtension(samplerFixtureMethod, persistentSampleManager));

        // 2. apply BeanConverters from testMethod. the ones from the testMethod override the ones from the TestFixture.
        applyAnnotatedBeanConverterExtension(testMethod, persistentSampleManager);
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
        final BeanConverterExtension extension = JUnitSamplerUtils.instantiate(beanConverterExtensionClass);
        persistentSampleManager.addBeanExtension(extension);
    }

}
