package de.ppi.deepsampler.persistence.json.extension;

import com.fasterxml.jackson.databind.JsonSerializer;

public class SerializationExtension<T> {
    private final Class<? extends T> cls;
    private final JsonSerializer<T> jsonSerializer;

    public SerializationExtension(Class<? extends T> cls, JsonSerializer<T> jsonSerializer) {
        this.cls = cls;
        this.jsonSerializer = jsonSerializer;
    }

    public Class<? extends T> getCls() {
        return cls;
    }

    public JsonSerializer<T> getJsonSerializer() {
        return jsonSerializer;
    }
}
