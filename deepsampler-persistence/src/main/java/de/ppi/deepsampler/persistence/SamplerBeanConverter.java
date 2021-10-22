/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.bean.ext.CollectionExtension;
import de.ppi.deepsampler.persistence.bean.ext.MapPrimitiveKeyExtension;
import de.ppi.deepsampler.persistence.bean.ext.JavaTimeExtension;
import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;

public class SamplerBeanConverter {

    private SamplerBeanConverter() {
        // static only
    }

    static PersistentBeanConverter create() {
        PersistentBeanConverter persistentBeanConverter = new PersistentBeanConverter();
        persistentBeanConverter.addExtension(new JavaTimeExtension());
        persistentBeanConverter.addExtension(new MapPrimitiveKeyExtension());
        persistentBeanConverter.addExtension(new CollectionExtension());
        return persistentBeanConverter;
    }
}
