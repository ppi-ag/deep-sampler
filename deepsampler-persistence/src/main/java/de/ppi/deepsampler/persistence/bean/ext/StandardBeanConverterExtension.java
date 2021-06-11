/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;

import java.lang.reflect.Type;

public abstract class StandardBeanConverterExtension implements BeanConverterExtension {


    @Override
    public boolean skip(Type beanType) {
        return false;
    }

    @Override
    public Object convert(Object originalBean, PersistentBeanConverter persistentBeanConverter) {
        return originalBean;
    }

    @Override
    public <T> T revert(Object persistentBean, Type type, PersistentBeanConverter persistentBeanConverter) {
        return (T) persistentBean;
    }
}
