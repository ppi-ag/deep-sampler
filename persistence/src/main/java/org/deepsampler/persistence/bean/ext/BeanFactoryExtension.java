package org.deepsampler.persistence.bean.ext;


import org.deepsampler.persistence.model.PersistentBean;

public interface BeanFactoryExtension {
    boolean isProcessable(Class<?> beanCls);
    boolean skip(Class<?> beanCls);

    PersistentBean toBean(Object bean);
    <T> T ofBean(PersistentBean bean, Class<T> target);
}
