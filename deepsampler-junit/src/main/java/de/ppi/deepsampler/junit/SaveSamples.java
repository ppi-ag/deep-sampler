/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @{@link SaveSamples}-annotation may be used at any test method as a convenient way to record samples from a
 * running test. The recorded samples are saved as a JSON-file and are usually loaded by @{@link LoadSamples}
 * <p>
 * The file's name is composed of two parts: [rootPath][fileName]. DeepSampler generates a default name using
 * the test class and test method. In most cases this will suffice, however you can change the path in detail:
 * <ul>
 *     <li>rootPath: The root path for relative packagePaths is by default './'. It can be changed, using the annotation
 *  *     {@link SampleRootPath}.</li>
 *     <li>value(): The concrete name of the JSON-file. If omitted, the name of the test class, including it's package,
 *     and the test method is used.</li>
 * </ul>
 * <p>
 * This annotation must be used in combination with @{@link UseSamplerFixture}.
 * <p>
 * It is possible to register customisations of the serialization-process using the annotations
 * {@link UseJsonSerializer} and @{@link UseBeanConverterExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SaveSamples {


    /**
     * The name of the json file. Default is the package of the test class, followed by the name of the test class,
     * followed by the name of the test method.
     *
     * @return the name of the sample JSON file.
     */
    String value() default AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED;
}
