/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JavaTimeExtension extends StandardBeanFactoryExtension {

    @Override
    public boolean isProcessable(Class<?> beanCls) {
        return LocalDateTime.class.isAssignableFrom(beanCls) || LocalDate.class.isAssignableFrom(beanCls);
    }

    @Override
    public boolean skip(Class<?> beanCls) {
        return true;
    }
}
