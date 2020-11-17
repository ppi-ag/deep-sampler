/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.model;


import java.util.List;

public interface PersistentActualSample {
    List<PersistentMethodCall> getAllCalls();
}
