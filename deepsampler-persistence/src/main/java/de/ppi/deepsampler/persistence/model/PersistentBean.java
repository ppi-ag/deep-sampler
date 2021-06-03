/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.model;

import java.util.Map;

public interface PersistentBean extends Persistable {
    void setValues(Map<String, Object> values);
    Map<String, Object> getValues();
    Object getValue(String key);
}
