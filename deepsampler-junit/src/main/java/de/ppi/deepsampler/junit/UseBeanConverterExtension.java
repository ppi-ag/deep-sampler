/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DeepSampler's persistence runs through two steps. First, Objects are converted to abstract {@link de.ppi.deepsampler.persistence.model.PersistentBean}s and second
 * these beans are passed to Jackson, the actual Json-Serializer.
 * DeepSampler is able to convert most objects to {@link de.ppi.deepsampler.persistence.model.PersistentBean}s out of the box, but sometimes objects need a special
 * conversion. This can be done using custom  {@link BeanConverterExtension}s.
 *
 * This annotation is used to activate a custom {@link BeanConverterExtension}. The annotation can be placed on
 * <ul>
 *     <li>a test method</li>
 *     <li>the class that declares the test method</li>
 *     <li>the method {@link SamplerFixture#defineSamplers()}</li>
 *     <li>the class that defines the method defineSamplers</li>
 * </ul>
 *
 * If {@link UseBeanConverterExtension} is used at more than one of the aforementioned places, and several of them define {@link BeanConverterExtension}s
 * for the same type, the ones on top of the aforementioned list, override the lower ones.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UseBeanConverterExtension {

    Class<? extends BeanConverterExtension>[] value();
}
