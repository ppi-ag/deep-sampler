package de.ppi.deepsampler.persistence.bean.ext;

import java.util.Collection;
import java.util.Map;

public class CollectionMapExtension extends StandardBeanConverterExtension {

    @Override
    public boolean isProcessable(Class<?> beanCls) {
        return Collection.class.isAssignableFrom(beanCls) ||
                Map.class.isAssignableFrom(beanCls);
    }

    @Override
    public boolean skip(Class<?> beanCls) {
        return isProcessable(beanCls);
    }
}
