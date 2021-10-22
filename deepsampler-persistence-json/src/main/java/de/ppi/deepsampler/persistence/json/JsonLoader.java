/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.Module;
import de.ppi.deepsampler.persistence.json.error.JsonPersistenceException;
import de.ppi.deepsampler.persistence.json.extension.DeserializationExtension;
import de.ppi.deepsampler.persistence.model.PersistentModel;
import de.ppi.deepsampler.persistence.json.model.JsonSampleModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JsonLoader extends JsonOperator {

    public JsonLoader(PersistentResource persistentResource) {
        super(persistentResource, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public JsonLoader(PersistentResource persistentResource, List<DeserializationExtension<?>> deserializerList, List<Module> moduleList) {
        super(persistentResource, deserializerList, Collections.emptyList(), moduleList);
    }

    public PersistentModel load() {
        try {
            InputStream in = Objects.requireNonNull(getPersistentResource().readAsStream());
            return createObjectMapper().readValue(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), JsonSampleModel.class);
        } catch (final IOException e) {
            throw new JsonPersistenceException("It was not possible to deserialize/read the file", e);
        }
    }
}
