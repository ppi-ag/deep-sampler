package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.json.JsonSourceManager;

public class TestPersistenceManagerProvider implements PersistentSampleManagerProvider {

    @Override
    public JsonSourceManager.Builder configurePersistentSampleManager() {
        return JsonSourceManager.builder();
    }
}
