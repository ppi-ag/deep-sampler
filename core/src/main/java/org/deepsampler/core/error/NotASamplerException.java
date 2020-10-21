package org.deepsampler.core.error;

/**
 * An Exception that is thrown if a pojo object is passed where instead a Sampler is expected. That usually happens when an object is used that
 * was not created by {@link org.deepsampler.core.api.Sampler#prepare(Class)} or injected by the annotation @PrepareSampler.
 */
public class NotASamplerException extends BaseException {

    /**
     *
     * @param cls The class of the object that was expected to ba a Sampler, but in fact was a pojo.
     */
    public NotASamplerException(Class<?> cls) {
        super("The class %s is not a Sampler. Please create a Sampler using Sampler.prepare() or " +
                "by adding the Annotation @PrepareSampler to a field if you use a JUnit-Extension.", cls.getName());
    }
}