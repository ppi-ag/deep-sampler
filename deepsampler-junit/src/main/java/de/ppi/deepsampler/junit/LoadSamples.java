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
 * The @{@link LoadSamples}-annotation may be used at any test method as a convenient way to load Samples from a
 * JSON-file. The JSON-file is usually created using @{@link SaveSamples}.
 * <p>
 * It is possible to load the file either from a local filesystem or from the classpath (property {@link LoadSamples#source()})
 * <p>
 * The filename may be defined using the property {@link LoadSamples#value()}. The default filename is composed using the
 * full qualified name of the test class and the test method.
 *
 * <p>
 * This annotation must be used in combination with @{@link UseSamplerFixture}.
 * <p>
 * It is possible to register some extensions to customize the deserialization using the annotations @{@link UseJsonDeserializer}
 * and @{@link UseBeanConverterExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoadSamples {


    /**
     * The name of the json file. If this property is omitted, the name will be composed like so:
     * [package of the test class]/[Test class name]_[test method].json
     * <p>
     * The root path of relative filenames for source
     *  <ul>
     *      <li>{@link FileSource#FILE_SYSTEM} is by default './'. It can be changed, using the annotation {@link SampleRootPath}.</li>
     *      <li>{@link FileSource#CLASSPATH} is the classpath itself. value() will be interpreted exactly as it is described by
     *         {@link ClassLoader#getResource(String)}. Since the ClassLoader is retrieved from the current test class, value()
     *         can also be defined relative to the test class. The annotation {@link SampleRootPath} is ignored.</li>
     *  * </ul>
     *
     * @return the name of the sample JSON file.
     */
    String value() default AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED;

    /**
     * Defines from where the file is loaded. Either the classpath or the vanilla filesystem can be used. Default is
     * {@link FileSource#FILE_SYSTEM}.
     *
     * @return The source of the file.
     */
    FileSource source() default FileSource.FILE_SYSTEM;
}
