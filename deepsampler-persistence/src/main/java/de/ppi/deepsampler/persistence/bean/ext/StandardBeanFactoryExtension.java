/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.model.PersistentBean;
import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;

public abstract class StandardBeanFactoryExtension implements BeanFactoryExtension {

    @Override
    public boolean skip(Class<?> beanCls) {
        return false;
    }

    @Override
    public PersistentBean toBean(Object bean) {
        return new PersistentBeanFactory().toPersistentBean(bean);
    }

    @Override
    public <T> T ofBean(PersistentBean bean, Class<T> cls) {
        return new PersistentBeanFactory().toOriginalBean(bean, cls);
    }
}
