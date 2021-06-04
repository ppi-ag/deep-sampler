/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.model.PersistentBean;
import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;

import java.lang.reflect.Type;

public abstract class StandardBeanFactoryExtension implements BeanFactoryExtension {

    @Override
    public boolean skip(Type beanType) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object toBean(Object targetBean, Type targetBeanType) {
        return new PersistentBeanFactory().toBean(targetBean, targetBeanType);
    }

    @Override
    public <T> T ofBean(Object persistentBean, Type targetBeanType) {
        return new PersistentBeanFactory().createValueFromPersistentBean(persistentBean, targetBeanType);
    }
}
