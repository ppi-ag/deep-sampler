package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;

public @interface UseBeanConverterExtension {

    Class<BeanConverterExtension>[] value();
}
