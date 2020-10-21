package org.deepsampler.junit4;

import org.deepsampler.core.api.Sampler;
import org.deepsampler.junit.TestReflectionUtils;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * As a convenient alternative, Samplers may be prepared by annotating properties in test classes with {@link org.deepsampler.junit.PrepareSampler} instead of using
 * {@link org.deepsampler.core.api.Sampler#prepare(Class)}. In Junit4 test cases the annotation is interpreted by this Rule.
 *
 * The Rule also clears Samplers that might have been created by preceding tests before a new test method is started.
 * This would otherwise have to be done by calling {@link Sampler#clear()} manually in each test.
 *
 * The Rule is activated by adding a property to the test class like this: {@code @Rule public DeepSamplerRule deepSamplerRule = new DeepSamplerRule();}
 */
public class DeepSamplerRule implements MethodRule {


    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        TestReflectionUtils.getDeclaredAndInheritedFields(target.getClass())//
            .filter(TestReflectionUtils::shouldBeSampled)//
            .forEach(field -> TestReflectionUtils.assignNewSamplerToField(target, field));

        Sampler.clear();

        return base;
    }

}