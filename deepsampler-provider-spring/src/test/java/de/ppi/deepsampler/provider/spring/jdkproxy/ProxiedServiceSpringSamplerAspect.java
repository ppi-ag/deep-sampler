/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring.jdkproxy;

import de.ppi.deepsampler.provider.spring.SpringSamplerAspect;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * A custom {@link SpringSamplerAspect} that defines which classes should be intercepted, i.e. which classes can be stubbed.
 */
@Aspect
public class ProxiedServiceSpringSamplerAspect extends SpringSamplerAspect {

    /**
     * This PointCut expression selects a single bean, that is a jdk proxy.
     * <p>
     * See https://docs.spring.io/spring-framework/docs/2.0.x/reference/aop.html
     */
    @Pointcut("target(de.ppi.deepsampler.provider.spring.jdkproxy.ProxiedTestService)")
    @Override
    public void include() {
    }

    /**
     * {@link SpringSamplerAspect#defaultPointCut()} excludes final classes from the PointCut expression by default, because
     * final classes usually cannot be intercepted by Soring AOP. JKD-Proxies are an exception, they can be
     * intercepted, although they are final. In order to test if jdk proxies can be stubbed, we need to override
     * the defaultPointCut without the exclusion of final classes.
     */
    @Pointcut("!@within(org.springframework.context.annotation.Configuration) " // excludes all SpringConfigs
            + "&& !@within(org.aspectj.lang.annotation.Aspect) " // Excludes all Aspects by excluding classes annotated with @Aspect
            + "&& !within(is(EnumType)) ") // Excludes all Enums
    @SuppressWarnings("unused") // Method is called generically by Spring, so the compiler believes it would be unused.
    @Override
    public void defaultPointCut() {
    }
}
