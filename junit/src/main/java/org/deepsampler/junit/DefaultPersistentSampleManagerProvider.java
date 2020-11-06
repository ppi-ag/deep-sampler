package org.deepsampler.junit;

import org.deepsampler.persistence.json.JsonSourceManager;

public class DefaultPersistentSampleManagerProvider implements PersistentSampleManagerProvider {

    @Override
    public JsonSourceManager.Builder configurePersistentSampleManager() {
        return JsonSourceManager.builder();
    }
}
