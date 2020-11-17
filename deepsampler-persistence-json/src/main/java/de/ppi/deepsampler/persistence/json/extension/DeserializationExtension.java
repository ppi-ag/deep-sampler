/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.extension;

import com.fasterxml.jackson.databind.JsonDeserializer;

public class DeserializationExtension<T> {

    private final Class<T> cls;
    private final JsonDeserializer<? extends T> jsonDeserializer;

    public DeserializationExtension(Class<T> cls, JsonDeserializer<? extends T> jsonDeserializer) {
        this.cls = cls;
        this.jsonDeserializer = jsonDeserializer;
    }

    public Class<T> getCls() {
        return cls;
    }

    public JsonDeserializer<? extends T> getJsonDeserializer() {
        return jsonDeserializer;
    }
}
