package org.deepsampler.junit;

import org.deepsampler.persistence.json.JsonSourceManager;

public class TestPersistenceManagerProvider implements PersistentSampleManagerProvider {

    @Override
    public JsonSourceManager.Builder configurePersistentSampleManager() {
        return JsonSourceManager.builder();
    }
}
