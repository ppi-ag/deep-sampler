/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.model.Persistable;
import de.ppi.deepsampler.persistence.model.PersistentBean;
import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;

import java.util.List;

public abstract class StandardBeanFactoryExtension implements BeanFactoryExtension {

    @Override
    public boolean skip(Class<?> beanCls) {
        return false;
    }

    @Override
    public Persistable toBean(Object bean) {
        return new PersistentBeanFactory().toBean(bean);
    }

    @Override
    public <T> T ofBean(Persistable bean, Class<T> cls) {
        return new PersistentBeanFactory().createValueFromPersistentBean((PersistentBean) bean, cls);
    }
}
