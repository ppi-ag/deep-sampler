package de.ppi.deepsampler.junit;

public class AnnotationConstants {

    /**
     * Default values in annotations must not be null (given by the java spec). However, we want to calculate
     * default values if the user doesn't provide a value. This calculation cannot be done inside the annotation,
     * therefore we need a way to communicate, that a default value was not provided. We do this using this constant
     * as a replacement for null.
     */
    public static final String DEFAULT_VALUE_MUST_BE_CALCULATED = "`v°°v´$de.ppi.deepsampler.junit.AnnotationConstants$$$DefaultValueMustBeCalculated$";

}
