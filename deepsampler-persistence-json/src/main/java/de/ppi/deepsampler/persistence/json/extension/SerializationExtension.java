/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.extension;

import com.fasterxml.jackson.databind.JsonSerializer;

public class SerializationExtension<T> {
    private final Class<? extends T> typeToSerialize;
    private final JsonSerializer<T> jsonSerializer;

    public SerializationExtension(final Class<? extends T> typeToSerialize, final JsonSerializer<T> jsonSerializer) {
        this.typeToSerialize = typeToSerialize;
        this.jsonSerializer = jsonSerializer;
    }

    public Class<? extends T> getTypeToSerialize() {
        return typeToSerialize;
    }

    public JsonSerializer<T> getJsonSerializer() {
        return jsonSerializer;
    }
}
