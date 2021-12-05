/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.model;

public interface PersistentMethodCall {
    PersistentParameter getPersistentParameter();
    Object getPersistentReturnValue();
}
