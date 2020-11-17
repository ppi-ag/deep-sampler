/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.bean.ext.BeanFactoryExtension;
import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;

public class PersistentSamplerContext {
    private final PersistentBeanFactory persistentBeanFactory = SamplerBeanFactory.create();

    public void addBeanFactoryExtension(BeanFactoryExtension beanFactoryExtension) {
        persistentBeanFactory.addExtension(beanFactoryExtension);
    }

    public PersistentBeanFactory getPersistentBeanFactory() {
        return persistentBeanFactory;
    }
}
