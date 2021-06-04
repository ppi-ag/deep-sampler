/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.bean.ext.ArrayExtension;
import de.ppi.deepsampler.persistence.bean.ext.JavaTimeExtension;
import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;
import de.ppi.deepsampler.persistence.bean.ext.CollectionExtension;

public class SamplerBeanFactory {

    private SamplerBeanFactory() {
        // static only
    }

    static PersistentBeanFactory create() {
        PersistentBeanFactory persistentBeanFactory = new PersistentBeanFactory();
        persistentBeanFactory.addExtension(new JavaTimeExtension());
        persistentBeanFactory.addExtension(new CollectionExtension());
        persistentBeanFactory.addExtension(new ArrayExtension());
        //persistentBeanFactory.addExtension(new CollectionMapExtension());
        return persistentBeanFactory;
    }
}
