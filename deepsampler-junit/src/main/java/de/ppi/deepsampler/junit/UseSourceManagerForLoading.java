/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.api.SourceManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Extensions may define their own annotations, to load samples for test methods. Annotations, that
 * tell DeepSampler to load samples, must be annotated with this meta-annotation {@link UseSourceManagerForLoading}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface UseSourceManagerForLoading {

    /**
     * Defines the {@link SourceManagerFactory} that will be used to create the {@link de.ppi.deepsampler.persistence.api.SourceManager}
     * that will load the samples.
     * @return A {@link SourceManagerFactory}
     */
    @SuppressWarnings("java:S1452") // The generic wildcard is necessary because we want to allow all kinds of SourceManagers
    Class<? extends SourceManagerFactory<? extends SourceManager>> value();

}
