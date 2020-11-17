/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.error;

import de.ppi.deepsampler.core.api.Sampler;

/**
 * An Exception that is thrown if a pojo object is passed where instead a Sampler is expected. That usually happens when an object is used that
 * was not created by {@link Sampler#prepare(Class)} or injected by the annotation @PrepareSampler.
 */
public class NotASamplerException extends BaseException {

    /**
     *
     * @param cls The class of the object that was expected to ba a Sampler, but in fact was a pojo.
     */
    public NotASamplerException(final Class<?> cls) {
        super("The class %s is not a Sampler. Please create a Sampler using Sampler.prepare() or " +
                "by adding the Annotation @PrepareSampler to a field if you use a JUnit-Extension.", cls.getName());
    }

    /**
     *
     * @param message An explanation of the Exception.
     * @param cause The original Exception.
     */
    public NotASamplerException(final String message, final Exception cause) {
        super(message, cause);
    }

    /**
     *
     * @param message An explanation of the Exception.
     */
    public NotASamplerException(final String message) {
        super(message);
    }
}
