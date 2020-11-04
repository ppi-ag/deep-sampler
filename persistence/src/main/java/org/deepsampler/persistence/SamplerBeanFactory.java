package org.deepsampler.persistence;

import org.deepsampler.persistence.bean.PersistentBeanFactory;
import org.deepsampler.persistence.bean.ext.JavaTimeExtension;

public class SamplerBeanFactory {

    private SamplerBeanFactory() {
        // static only
    }

    static PersistentBeanFactory create() {
        PersistentBeanFactory persistentBeanFactory = new PersistentBeanFactory();
        persistentBeanFactory.addExtension(new JavaTimeExtension());
        return persistentBeanFactory;
    }
}
