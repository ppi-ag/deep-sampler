package org.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.Module;
import org.deepsampler.persistence.json.error.JsonPersistenceException;
import org.deepsampler.persistence.json.extension.DeserializationExtension;
import org.deepsampler.persistence.json.model.JsonSampleModel;
import org.deepsampler.persistence.json.model.PersistentModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class JsonLoader extends JsonOperator {

    public JsonLoader(Path pathToJson) {
        super(pathToJson, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public JsonLoader(Path pathToJson, List<DeserializationExtension<?>> deserializerList, List<Module> moduleList) {
        super(pathToJson, deserializerList, Collections.emptyList(), moduleList);
    }

    public PersistentModel load(PersistentSamplerContext persistentSamplerContext) {
        try {
            return createObjectMapper().readValue(Files.newBufferedReader(getPath()), JsonSampleModel.class);
        } catch (final IOException e) {
            throw new JsonPersistenceException("It was not possible to deserialize/read the file", e);
        }
    }
}
