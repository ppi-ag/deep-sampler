package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UseBeanConverterExtension {

    Class<? extends BeanConverterExtension>[] value();
}
