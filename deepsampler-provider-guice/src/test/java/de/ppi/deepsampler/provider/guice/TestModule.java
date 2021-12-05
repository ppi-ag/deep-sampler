/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.guice;

import com.google.inject.AbstractModule;
import de.ppi.deepsampler.provider.testservices.DecoupledTestService;
import de.ppi.deepsampler.provider.testservices.DecoupledTestServiceImpl;

/**
 * A Module that defines a simple binding and - more important in this context - installs the {@link DeepSamplerModule}.
 */
public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DecoupledTestService.class).to(DecoupledTestServiceImpl.class);
        install(new DeepSamplerModule());
    }
}
