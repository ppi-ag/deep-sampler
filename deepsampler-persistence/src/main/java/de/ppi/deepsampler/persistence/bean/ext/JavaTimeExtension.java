/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;


import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class JavaTimeExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(Class<?> beanClass, ParameterizedType beanType) {
        return LocalDateTime.class.isAssignableFrom(beanClass) || LocalDate.class.isAssignableFrom(beanClass)
                || Date.class.isAssignableFrom(beanClass);
    }

    @Override
    public boolean skip(Class<?> beanClass, ParameterizedType beanType) {
        return true;
    }
}
