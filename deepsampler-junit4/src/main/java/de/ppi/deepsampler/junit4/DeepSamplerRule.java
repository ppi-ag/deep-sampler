package de.ppi.deepsampler.junit4;

import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.junit.JUnitPluginUtils;
import de.ppi.deepsampler.junit.PrepareSampler;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * As a convenient alternative, Samplers may be prepared by annotating properties in test classes with {@link PrepareSampler} instead of using
 * {@link Sampler#prepare(Class)}. In Junit4 test cases the annotation is interpreted by this Rule.
 * <p>
 * The Rule also clears Samplers that might have been created by preceding tests before a new test method is started.
 * This would otherwise have to be done by calling {@link Sampler#clear()} manually in each test.
 * <p>
 * The Rule is activated by adding a property to the test class like this: {@code @Rule public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();}
 */
public class DeepSamplerRule implements MethodRule {


    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Sampler.clear();

                JUnitPluginUtils.injectSamplers(target);
                JUnitPluginUtils.applyTestFixture(method.getMethod());
                JUnitPluginUtils.loadSamples(method.getMethod());

                base.evaluate();

                JUnitPluginUtils.saveSamples(method.getMethod());
            }
        };
    }

}
