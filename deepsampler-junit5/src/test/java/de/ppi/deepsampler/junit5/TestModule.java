/*
 *
 *  * Copyright 2020 PPI AG (Hamburg, Germany)
 *  * This program is made available under the terms of the MIT License.
 *
 */

package de.ppi.deepsampler.junit5;

import com.google.inject.AbstractModule;
import de.ppi.deepsampler.provider.guice.DeepSamplerModule;

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new DeepSamplerModule());
    }
}
