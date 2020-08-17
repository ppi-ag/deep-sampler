package org.deepmock.provider.standalone;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class DeepMockModule extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.any(), new GuiceBehaviorInterceptor());
    }
}
