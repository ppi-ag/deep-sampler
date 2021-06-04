/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class JavaTimeExtension extends StandardBeanFactoryExtension {

    @Override
    public boolean isProcessable(Type beanCls) {
        return beanCls instanceof Class
                && (LocalDateTime.class.isAssignableFrom((Class<?>) beanCls)
                || LocalDate.class.isAssignableFrom((Class<?>) beanCls)
                || Date.class.isAssignableFrom((Class<?>) beanCls));
    }

    @Override
    public boolean skip(Type beanType) {
        return true;
    }
}
