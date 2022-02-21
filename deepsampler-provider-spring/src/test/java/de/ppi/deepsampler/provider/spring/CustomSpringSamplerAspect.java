/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring;

import de.ppi.deepsampler.provider.common.SamplerAspectTest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * A custom {@link SpringSamplerAspect} that defines which classes should be intercepted, i.e. which classes can be stubbed.
 */
@Aspect
public class CustomSpringSamplerAspect extends SpringSamplerAspect {

    /**
     * The Pointcut defines, that all classes in the package {@code de.ppi.deepsampler.provider.common} and all sub packages
     * will be intercepted.
     * <br>
     * The Pointcut is defined in a way that will exclude classes in {@code de.ppi.deepsampler.provider..*}. This is important
     * for the test {@link SamplerAspectTest#serviceCanBeCastedFromInterfaceToConcrete()}. This test casts a
     * autowired service from its interface to its concrete class. This is not possible if the class has been intercepted.
     * If the exclusion of {@code }de.ppi.deepsampler.provider..*} doesn't work, the test will turn red.
     */
    @Pointcut("within(de.ppi.deepsampler.provider.common..*)")
    public void include() {}
}
