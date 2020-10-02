package org.deepsampler.core.model;

import java.io.Serializable;

public interface ReturnValueSupplier extends Serializable {
    Object supply();
}
