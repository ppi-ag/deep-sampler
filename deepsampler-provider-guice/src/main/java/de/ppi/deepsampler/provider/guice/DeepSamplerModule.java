/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;

import java.lang.reflect.Modifier;

public class DeepSamplerModule extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(new AnyInterceptableClass(), Matchers.any(), new GuiceSamplerAspect());
    }


    /**
     * We try to intercept all classes on the current classpath, but there are some classes (e.g final classes and enums)
     * that cannot be intercepted by guice. These classes are ignored by this {@link Matcher}.
     */
    private static class AnyInterceptableClass extends AbstractMatcher<Class> {

        @Override
        public boolean matches(Class aClass) {
            return !Modifier.isFinal(aClass.getModifiers()) && !aClass.isEnum();
        }
    }
}
