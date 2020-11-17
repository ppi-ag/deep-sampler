/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.model;

import java.util.Map;

public interface PersistentModel {
    String getId();
    Map<PersistentSampleMethod, PersistentActualSample> getSampleMethodToSampleMap();
}
