package org.deepsampler.provider.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class DeepSamplerModule extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.any(), new GuiceSamplerInterceptor());
    }
}
