package org.deepsampler.junit5;

import org.deepsampler.core.api.Sampler;
import org.deepsampler.junit.TestReflectionUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;


/**
 * As a convenient alternative, Samplers may be prepared by annotating properties in test classes with {@link org.deepsampler.junit.PrepareSampler} instead of using
 * {@link org.deepsampler.core.api.Sampler#prepare(Class)}. In Junit5 the annotation is interpreted by this JUnit Extension.
 *
 * The Extension also clears Samplers that might have been created by preceding tests before a new test method is started.
 * This would otherwise have to be done by calling {@link Sampler#clear()} manually in each test.
 *
 * The Extension is enabled by annotation a test class with {@code @ExtendWith(DeepSamplerExtension.class)}
 */
public class DeepSamplerExtension implements TestInstancePostProcessor, BeforeEachCallback {

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        TestReflectionUtils.getDeclaredAndInheritedFields(testInstance.getClass())//
            .filter(TestReflectionUtils::shouldBeSampled)//
            .forEach(field -> TestReflectionUtils.assignNewSamplerToField(testInstance, field));
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        Sampler.clear();
    }




}
