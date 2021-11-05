package de.ppi.deepsampler.persistence.bean;

import java.util.Map;

public class PolymorphicPersistentBean extends DefaultPersistentBean{
    private String polymorphicBeanType;
    public PolymorphicPersistentBean(){
        super();

    }
    public PolymorphicPersistentBean(final Map<String, Object> values, String realType) {

        super(values);
        polymorphicBeanType = realType;
    }

    public String getPolymorphicBeanType(){
        return polymorphicBeanType;
    }

}
