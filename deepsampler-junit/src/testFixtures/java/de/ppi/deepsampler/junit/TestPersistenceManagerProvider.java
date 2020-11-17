/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.persistence.json.JsonSourceManager;

public class TestPersistenceManagerProvider implements PersistentSampleManagerProvider {

    @Override
    public JsonSourceManager.Builder configurePersistentSampleManager() {
        return JsonSourceManager.builder();
    }
}
