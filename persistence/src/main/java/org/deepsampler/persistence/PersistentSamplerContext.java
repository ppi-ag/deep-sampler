package org.deepsampler.persistence;

import org.deepsampler.persistence.bean.PersistentBeanFactory;
import org.deepsampler.persistence.bean.ext.BeanFactoryExtension;

public class PersistentSamplerContext {
    private final PersistentBeanFactory persistentBeanFactory = SamplerBeanFactory.create();

    public void addBeanFactoryExtension(BeanFactoryExtension beanFactoryExtension) {
        persistentBeanFactory.addExtension(beanFactoryExtension);
    }

    public PersistentBeanFactory getPersistentBeanFactory() {
        return persistentBeanFactory;
    }
}
