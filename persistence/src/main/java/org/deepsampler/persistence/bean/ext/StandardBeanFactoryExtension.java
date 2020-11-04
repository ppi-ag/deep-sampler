package org.deepsampler.persistence.bean.ext;

import org.deepsampler.persistence.bean.PersistentBeanFactory;
import org.deepsampler.persistence.model.PersistentBean;

public abstract class StandardBeanFactoryExtension implements BeanFactoryExtension {

    @Override
    public boolean skip(Class<?> beanCls) {
        return false;
    }

    @Override
    public PersistentBean toBean(Object bean) {
        return new PersistentBeanFactory().toBean(bean);
    }

    @Override
    public <T> T ofBean(PersistentBean bean, Class<T> cls) {
        return new PersistentBeanFactory().createValueFromPersistentBean(bean, cls);
    }
}
