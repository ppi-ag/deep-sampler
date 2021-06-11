/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.ReflectionTools;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class JavaTimeExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(Type beanType) {
        Class<?> rawClass = ReflectionTools.getClass(beanType);
        return LocalDateTime.class.isAssignableFrom(rawClass) || LocalDate.class.isAssignableFrom(rawClass)
                || Date.class.isAssignableFrom(rawClass);
    }

    @Override
    public boolean skip(Type beanType) {
        return true;
    }
}
