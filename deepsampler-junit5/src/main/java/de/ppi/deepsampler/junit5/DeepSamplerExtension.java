/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;

import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.junit.JUnitPluginUtils;
import de.ppi.deepsampler.junit.PrepareSampler;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;


/**
 * As a convenient alternative, Samplers may be prepared by annotating properties in test classes with {@link PrepareSampler} instead of using
 * {@link Sampler#prepare(Class)}. In Junit5 the annotation is interpreted by this JUnit Extension.
 * <p>
 * The Extension also clears Samplers that might have been created by preceding tests before a new test method is started.
 * This would otherwise have to be done by calling {@link Sampler#clear()} manually in each test.
 * <p>
 * The Extension is enabled by annotation a test class with {@code @ExtendWith(DeepSamplerExtension.class)}
 */
public class DeepSamplerExtension implements TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        JUnitPluginUtils.injectSamplers(testInstance);
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        Sampler.clear();

        context.getTestMethod().ifPresent(testMethod -> {
            JUnitPluginUtils.applyTestFixture(testMethod);
            JUnitPluginUtils.loadSamples(testMethod);
        });
    }


    @Override
    public void afterEach(final ExtensionContext context) {
        context.getTestMethod().ifPresent(JUnitPluginUtils::saveSamples);
    }
}
