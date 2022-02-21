/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.extension;

import com.fasterxml.jackson.databind.JsonDeserializer;

public class DeserializationExtension<T> {

    private final Class<T> typeToSerialize;
    private final JsonDeserializer<T> jsonDeserializer;

    public DeserializationExtension(final Class<T> typeToSerialize, final JsonDeserializer<T> jsonDeserializer) {
        this.typeToSerialize = typeToSerialize;
        this.jsonDeserializer = jsonDeserializer;
    }

    public Class<T> getTypeToSerialize() {
        return typeToSerialize;
    }

    public JsonDeserializer<T> getJsonDeserializer() {
        return jsonDeserializer;
    }
}
