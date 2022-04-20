/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.api.SourceManager;

import java.lang.reflect.Method;

/**
 * DeepSampler allows extensions to define their own annotations for loading and saving samples. The annotation,
 * that loads samples must be annotated with the meta-annotation {@link UseSourceManagerForLoading}. The annotation,
 * that saves samples must be annotated with the meta-annotation {@link UseSourceManagerForSaving}. Both meta-annotations
 * define a {@link SourceManagerFactory}, that is used to create and configure the {@link SourceManager}
 * * A {@link SourceManager} is used to load or save samples in arbitrary formats, like JSON.
 *
 * A concrete {@link SourceManagerFactory} may use custom annotations to configure the {@link SourceManager}. These custom
 * annotations may sit on test methods, classes, or {@link SamplerFixture}s. The method
 * {@link JUnitSamplerUtils#loadAnnotationFromTestOrSampleFixture(Method, Class)} simplifies loading these annotations.
 *
 * @param <T>
 */
public interface SourceManagerFactory<T extends SourceManager> {

    /**
     * Creates and configures a {@link SourceManager}, that will be used to load samples.
     * @param testMethod a test method that is annotated with any custom annotation that is annotated with {@link UseSourceManagerForLoading}.
     *                   This meta-annotation tells DeepSampler, that the annotated annotation is a marker for loading samples
     *                   using this {@link SourceManagerFactory}.
     *                   The method may also be annotated with various custom annotations that can be used to configure the {@link SourceManager}.
     * @return A {@link SourceManager} that is able to load samples for the current test method.
     */
    T createSourceManagerToLoadSamples(Method testMethod);

    /**
     * Creates and configures a {@link SourceManager} that will be used to save samples.
     * @param testMethod a test method that is annotated with any custom annotation that is annotated with {@link UseSourceManagerForSaving}.
     *                   This meta-annotation tells DeepSampler, that the annotated annotation is a marker for saving samples
     *                   using this {@link SourceManagerFactory}.
     *                   The method may also be annotated with various custom annotations that can be used to configure the {@link SourceManager}.
     * @return A {@link SourceManager} that is able to save samples for the current test method.
     */
    T createSourceManagerToSaveSamples(Method testMethod);
}
