package de.ppi.deepsampler.persistence.bean;

import java.util.Map;

/**
 * This class implements the necessary information to persist a bean which has a polymorphic structure. it contains the information of the "true" type not only the type of the parent.
 */
public class PolymorphicPersistentBean extends DefaultPersistentBean {
    /**
     * Contains the true type of the object for deserialization purpose as string
     */
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
