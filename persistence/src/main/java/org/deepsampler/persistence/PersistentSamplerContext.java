package org.deepsampler.persistence.json;

import org.deepsampler.persistence.json.bean.ext.BeanFactoryExtension;
import org.deepsampler.persistence.json.bean.PersistentBeanFactory;

public class PersistentSamplerContext {
    private final PersistentBeanFactory persistentBeanFactory = SamplerBeanFactory.create();

    void addBeanFactoryExtension(BeanFactoryExtension beanFactoryExtension) {
        persistentBeanFactory.addExtension(beanFactoryExtension);
    }

    public PersistentBeanFactory getPersistentBeanFactory() {
        return persistentBeanFactory;
    }
}
