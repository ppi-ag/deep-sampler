package org.deepsampler.persistence.json;

import org.deepsampler.persistence.json.bean.PersistentBeanFactory;
import org.deepsampler.persistence.json.bean.ext.JavaTimeExtension;

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
