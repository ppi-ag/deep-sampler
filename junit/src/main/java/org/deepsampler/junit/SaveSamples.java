package org.deepsampler.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SaveSamples {

    Class<? extends PersistentSampleManagerProvider> persistenceManagerProvider() default DefaultPersistentSampleManagerProvider.class;

    String file() default "";
}
