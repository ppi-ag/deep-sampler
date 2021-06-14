/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;

import java.lang.reflect.ParameterizedType;

public abstract class StandardBeanConverterExtension implements BeanConverterExtension {


    @Override
    public boolean skip(Class<?> beanClass, ParameterizedType beanType) {
        return false;
    }

    @Override
    public Object convert(Object originalBean, ParameterizedType beanType, PersistentBeanConverter persistentBeanConverter) {
        return originalBean;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T revert(Object persistentBean, Class<T> targetClass, ParameterizedType type, PersistentBeanConverter persistentBeanConverter) {
        return (T) persistentBean;
    }
}
