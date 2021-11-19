package de.ppi.deepsampler.persistence.bean;

import java.util.Map;

public class PolymorphicPersistentBean extends DefaultPersistentBean {
    private String polymorphicBeanType;

    public PolymorphicPersistentBean() {
    }

    public PolymorphicPersistentBean(final Map<String, Object> values, String polymorphicBeanType) {
        super(values);
        this.polymorphicBeanType = polymorphicBeanType;
    }

    public String getPolymorphicBeanType() {
        return polymorphicBeanType;
    }

}
