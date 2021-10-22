/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link LoadSamples}-annotation may be used at any test method as a convenient way to load Samples from a JSON-file. The JSON-file is usually created
 * using {@link SaveSamples}.
 * <p>
 * It is possible to load the file either from a local file system (property {@link LoadSamples#file()}) or from the classpath (property {@link LoadSamples#classPath()})
 * <p>
 * If no file-name is provided, DeepSampler loads the file from the classpath, expecting that the file is located in the same package as the test class
 * and is named in the form {@code [simple class name]_[simple method name].json}. E.g. given a method {@code org.project.MyTest#testIt()} the expected file name is
 * {@code /org/project/MyTest_testIt.json}
 * <p>
 * This annotation must be used in combination with {@link UseSamplerFixture}.
 * <p>
 * The serialisation is done in two steps: First, the object may be converted to an abstract {@link de.ppi.deepsampler.persistence.model.PersistentBean}. This is done
 * to omit type information in the JSON-Files. Primitive types, arrays, {@link java.util.List}s and {@link java.util.Map}s are usually not converted to a
 * {@link de.ppi.deepsampler.persistence.model.PersistentBean}. They are passed to the second step unchanged. The second step is the JSON-serialisation by Jackson,
 * which may include some {@link com.fasterxml.jackson.databind.JsonSerializer}s
 * <p>
 * It is possible to register some extensions to customize the serialisation using the annotations @{@link UseJsonSerializer}
 * and {@link UseBeanConverterExtension}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoadSamples {


    /**
     * If this property is provided, the Sampler-file will be loaded from the local file system using the provided file name.
     *
     * @return the path to a Sampler-JSON-file.
     */
    String file() default "";

    /**
     * If this property is provided, the Sampler-file will be loaded from the classpath using the provided resource-name.
     *
     * @return the path to a Sampler-JSON-file. The path is resolved relative to the test class.
     */
    String classPath() default "";
}
