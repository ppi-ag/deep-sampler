/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;
import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;

public class PersistentSamplerContext {
    private final PersistentBeanConverter persistentBeanConverter = SamplerBeanConverter.create();

    public void addBeanConverterExtension(BeanConverterExtension beanConverterExtension) {
        persistentBeanConverter.addExtension(beanConverterExtension);
    }

    public PersistentBeanConverter getPersistentBeanConverter() {
        return persistentBeanConverter;
    }
}
