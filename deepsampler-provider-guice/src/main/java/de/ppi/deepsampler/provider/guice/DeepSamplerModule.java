/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class DeepSamplerModule extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.any(), new GuiceSamplerInterceptor());
    }
}
