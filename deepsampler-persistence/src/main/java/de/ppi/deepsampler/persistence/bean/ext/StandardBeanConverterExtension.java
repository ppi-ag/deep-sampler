/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.model.PersistentBean;
import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;

public abstract class StandardBeanConverterExtension implements BeanConverterExtension {

    @Override
    public boolean skip(Class<?> beanCls) {
        return false;
    }

    @Override
    public PersistentBean convert(Object bean) {
        return new PersistentBeanConverter().convert(bean);
    }

    @Override
    public <T> T revert(PersistentBean bean, Class<T> cls) {
        return new PersistentBeanConverter().revert(bean, cls);
    }
}
