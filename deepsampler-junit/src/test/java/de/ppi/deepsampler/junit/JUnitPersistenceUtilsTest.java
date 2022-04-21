/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JUnitPersistenceUtilsTest {

    @Test
    void metaAnnotationCanBeFound() throws NoSuchMethodException {
        // ✋ GIVEN
        final Method annotatedMethod = AnnotatedClass.class.getMethod("annotatedMethod");

        // 🧪 WHEN
        final Optional<MetaAnnotation> hopefullyMetaAnnotation = JUnitSamplerUtils.getMetaAnnotation(annotatedMethod, MetaAnnotation.class);

        // 🔬 THEN
        assertThat(hopefullyMetaAnnotation).isPresent();
    }




    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    private @interface MetaAnnotation {}

    @MetaAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface AnnotatedAnnotation {}

    private static class AnnotatedClass {
        @AnnotatedAnnotation
        public void annotatedMethod() {}
    }

}
