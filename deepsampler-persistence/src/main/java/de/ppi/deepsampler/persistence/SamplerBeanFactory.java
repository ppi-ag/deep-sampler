package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.bean.ext.JavaTimeExtension;
import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;

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
