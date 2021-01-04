/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.bean.ext.CollectionMapExtension;
import de.ppi.deepsampler.persistence.bean.ext.JavaTimeExtension;
import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;

public class SamplerBeanFactory {

    private SamplerBeanFactory() {
        // static only
    }

    static PersistentBeanFactory create() {
        PersistentBeanFactory persistentBeanFactory = new PersistentBeanFactory();
        persistentBeanFactory.addExtension(new JavaTimeExtension());
        persistentBeanFactory.addExtension(new CollectionMapExtension());
        return persistentBeanFactory;
    }
}
