package org.deepsampler.persistence.model;

import java.util.Map;

public interface PersistentBean {
    void setValues(Map<String, Object> values);
    Map<String, Object> getValues();
    Object getValue(String key);
}
