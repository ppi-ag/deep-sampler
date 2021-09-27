/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;


import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class JavaTimeExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(final Class<?> beanClass, final ParameterizedType beanType) {
        return LocalDateTime.class.isAssignableFrom(beanClass) || LocalDate.class.isAssignableFrom(beanClass)
                || Date.class.isAssignableFrom(beanClass) || Instant.class.isAssignableFrom(beanClass);
    }

    @Override
    public boolean skip(final Class<?> beanClass, final ParameterizedType beanType) {
        return true;
    }
}
