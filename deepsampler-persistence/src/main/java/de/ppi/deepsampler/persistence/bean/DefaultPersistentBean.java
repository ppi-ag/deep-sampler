/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean;

import de.ppi.deepsampler.persistence.model.PersistentBean;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultPersistentBean implements PersistentBean {

    private Map<String, Object> values;

    public DefaultPersistentBean() {
        values = new LinkedHashMap<>();
    }

    public DefaultPersistentBean(final Map<String, Object> values) {
        this.values = new LinkedHashMap<>(values);
    }

    @Override
    public void setValues(final Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    @Override
    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public Object getValue(final String key) {
        return values.get(key);
    }
}
