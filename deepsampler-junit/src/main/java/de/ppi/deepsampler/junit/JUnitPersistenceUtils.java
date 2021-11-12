/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import de.ppi.deepsampler.core.error.BaseException;
import de.ppi.deepsampler.core.error.InvalidConfigException;
import de.ppi.deepsampler.persistence.api.PersistentSampleManager;
import de.ppi.deepsampler.persistence.api.PersistentSampler;
import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;
import de.ppi.deepsampler.persistence.json.JsonSourceManager;

import java.lang.reflect.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A collection of tools to load and save samples from Json-files. The configuration is done using annotations on test methods or the classes that declare the
 * methods.
 */
public class JUnitPersistenceUtils {

    public static final String DEFAULT_ROOT_PATH = "./";

    private JUnitPersistenceUtils() {
        // Private constructor since this utility class is not intended to be instantiated.
    }

    /**
     * Tests, that are annotated with {@link LoadSamples}, run in player mode. If a method is annotated with this annotation,
     * samples are loaded before the method is executed. The method must also be annotated with @{@link UseSamplerFixture}.
     * This annotation binds a {@link SamplerFixture} to the test, which declares the sampler that define which methods are stubbed.
     * These stubs are also the stubs that consume the samples from the Json-file.
     * <p>
     * The JSON-file can be recorded using @{@link SaveSamples}. See also {@link JUnitPersistenceUtils#saveSamples(Method)}.
     * <p>
     * The {@link SamplerFixture}, the testMethod, or the declaring class may also be annotated with {@link UseJsonDeserializer},
     * {@link UseJsonSerializer} and {@link UseBeanConverterExtension}
     *
     * @param testMethod samples will be loaded for the method testMethod if it is annotated with {@link LoadSamples}
     */
    public static void loadSamples(final Method testMethod) {
        final LoadSamples loadSamples = testMethod.getAnnotation(LoadSamples.class);

        if (loadSamples == null) {
            return;
        }

        final JsonSourceManager jsonSourceManager = createSourceManagerWithJsonSerializerExtensions(testMethod, loadSamples);

        PersistentSampleManager sampleManager = PersistentSampler.source(jsonSourceManager);
        applyBeanExtensionsFromTestCaseAndTestFixture(testMethod, sampleManager);

        sampleManager.load();
    }

    /**
     * Tests, that are annotated with @{@link SaveSamples} run in recording mode. This means, that stubs will call the original
     * methods and record the parameter values and return values of the method. The recorded sample is saved in a JSON-File,
     * that can be used to replay the sample if the test is run in player mode. The player mode is activated by @{@link LoadSamples}.
     * See also {@link JUnitPersistenceUtils#loadSamples(Method)}.
     * <p>
     * The annotation {@link UseSamplerFixture} must also be present on the method, or on the declaring class if {@link SaveSamples} is present.
     * {@link UseSamplerFixture} binds a {@link SamplerFixture} to the test.
     * <p>
     * The {@link SamplerFixture}, the testMethod, or the declaring class may also be annotated with {@link UseJsonDeserializer},
     * {@link UseJsonSerializer} and {@link UseBeanConverterExtension}
     *
     * @param testMethod samples will be recorded for the testMethod, if it is annotated with @{@link SaveSamples}.
     */
    public static void saveSamples(final Method testMethod) {
        final SaveSamples saveSamples = testMethod.getAnnotation(SaveSamples.class);

        if (saveSamples == null) {
            return;
        }

        final JsonSourceManager jsonSourceManager = createSourceManagerWithJsonSerializerExtensions(testMethod, saveSamples);

        PersistentSampleManager sampleManager = PersistentSampler.source(jsonSourceManager);
        applyBeanExtensionsFromTestCaseAndTestFixture(testMethod, sampleManager);

        sampleManager.record();
    }


    private static JsonSourceManager createSourceManagerWithJsonSerializerExtensions(final Method testMethod, final LoadSamples loadSamples) {
        final JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);
        Optional<SampleRootPath> rootPath = loadSampleRootPathFromTestOrSampleFixture(testMethod);

        switch (loadSamples.source()) {
            case FILE_SYSTEM:
                Path file = createPathForFilesystem(rootPath, loadSamples.packagePath(), loadSamples.fileName(), testMethod);
                return persistentSampleManagerBuilder.buildWithFile(file);
            case CLASSPATH:
            default:
                String classPathResource = createPathForClasspath(loadSamples, testMethod);
                return persistentSampleManagerBuilder.buildWithClassPathResource(classPathResource, testMethod.getDeclaringClass());
        }
    }

    private static Optional<SampleRootPath> loadSampleRootPathFromTestOrSampleFixture(Method testMethod) {
        SampleRootPath sampleRootPath = testMethod.getDeclaringClass().getAnnotation(SampleRootPath.class);

        if (sampleRootPath != null) {
            return Optional.of(sampleRootPath);
        }

        return JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(testMethod)
                .map(Object::getClass)
                .map(fixtureClass -> fixtureClass.getAnnotation(SampleRootPath.class));
    }

    /**
     * Creates a {@link Path} based on the three path elements. If any of them is missing a default will be used. The
     * path is concatenated like this: [rootPath][packagePath][fileName].
     *
     * @param rootPath    the annotatio {@link SampleRootPath} that is used to configure the root path. If it is not
     *                    supplied, the default ./ will be used.
     * @param packagePath A folder that is resolved under rootPath. If it is not supplied, the package of the class,
     *                    that declares testMethod is used. packagePath must not be null. An empty String will be
     *                    treated not-supplied. This is because the empty default value of
     *                    {@link LoadSamples#packagePath()} cannot be null.
     * @param fileName    A fileName thath is resoved under packagePath. If it is not supplied, the name of the testMethod,
     *                    ant the name of the class the declares testMethod, is used. fileName must not be null. An empty String will be
     *                    treated not-supplied. This is because the empty default value of
     *                    {@link LoadSamples#fileName()} ()} cannot be null.
     * @param testMethod  The test method that is running the current test. It is used to provide default values.
     * @return A path that is formatted to be used on the file system (in contrast to a path that is formatted to be used
     * on the classpath, see {@link JUnitPersistenceUtils#createPathForClasspath(LoadSamples, Method)}.
     */
    static Path createPathForFilesystem(Optional<SampleRootPath> rootPath, String packagePath, String fileName, Method testMethod) {
        Path file = rootPath.map(a -> Paths.get(a.value())).orElse(Paths.get(DEFAULT_ROOT_PATH));

        if (packagePath.equals(AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED)) {
            file = file.resolve(testMethod.getDeclaringClass().getPackage().getName().replace(".", "/"));
        } else {
            file = file.resolve(packagePath);
        }

        if (fileName.equals(AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED)) {
            file = file.resolve(getDefaultJsonFileName(testMethod));
        } else {
            file = file.resolve(fileName);
        }

        return file;
    }

    /**
     * <p>
     * Creates a Path that is used to load a sample file from the classpath. The path is based on two elements:
     * </p>
     * <p>
     * [packagePath][fileName]
     * </p>
     * <p>
     * Tf any of these two are missing, a default will be used.
     * </p>
     *
     * @param loadSamples provides the packagePath and the fileName. If packagePath is not supplied, the package of
     *                    testMethod is used. If fileName is not supplied, the name of testMethod and it's declaring
     *                    class is used.
     * @param testMethod the method that runns the current test. It is used to provide default values for path elements.
     * @return a path that can be used to load a sample file from the classpath.
     */
    static String createPathForClasspath(LoadSamples loadSamples, Method testMethod) {
        String file;

        if (loadSamples.packagePath().equals(AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED)) {
            file = "/" + testMethod.getDeclaringClass().getPackage().getName().replace(".", "/");
        } else {
            file = loadSamples.packagePath();
        }

        if (loadSamples.fileName().equals(AnnotationConstants.DEFAULT_VALUE_MUST_BE_CALCULATED)) {
            file += "/" + getDefaultJsonFileName(testMethod);
        } else {
            file += "/" + loadSamples.fileName();
        }

        return file;
    }

    private static JsonSourceManager createSourceManagerWithJsonSerializerExtensions(final Method testMethod, final SaveSamples saveSamples) {
        final JsonSourceManager.Builder persistentSampleManagerBuilder = JsonSourceManager.builder();

        applyJsonSerializersFromTestCaseAndTestFixture(testMethod, persistentSampleManagerBuilder);

        Optional<SampleRootPath> sampleRootPath = loadSampleRootPathFromTestOrSampleFixture(testMethod);
        final Path fileName = createPathForFilesystem(sampleRootPath, saveSamples.packagePath(), saveSamples.fileName(), testMethod);

        return persistentSampleManagerBuilder.buildWithFile(fileName);
    }

    private static String getDefaultJsonFileName(final Method testMethod) {
        return testMethod.getDeclaringClass().getSimpleName() + "_" + testMethod.getName() + ".json";
    }

    private static void applyBeanExtensionsFromTestCaseAndTestFixture(final Method testMethod, PersistentSampleManager persistentSampleManager) {
        // 1. Apply BeanConverter from TestFixture...
        JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(testMethod).map(JUnitPersistenceUtils::getDefineSamplersMethod)
                .ifPresent(samplerFixtureMethod -> applyAnnotatedBeanConverterExtension(samplerFixtureMethod, persistentSampleManager));

        // 2. apply BeanConverters from testMethod. the ones from the tetMethod override the ones from the TestFixture.
        applyAnnotatedBeanConverterExtension(testMethod, persistentSampleManager);
    }

    private static void applyJsonSerializersFromTestCaseAndTestFixture(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        // 1. Load serializers from SamplerFixture
        JUnitSamplerUtils.loadSamplerFixtureFromMethodOrDeclaringClass(testMethod).map(JUnitPersistenceUtils::getDefineSamplersMethod)
                .ifPresent(samplerFixtureMethod -> {
                    applyAnnotatedJsonSerializers(samplerFixtureMethod, persistentSampleManagerBuilder);
                    applyAnnotatedJsonDeserializers(samplerFixtureMethod, persistentSampleManagerBuilder);
                });

        // 2. Load serializers from testMethod. Serializers from testMethod override the ones from the TestFixture.
        applyAnnotatedJsonSerializers(testMethod, persistentSampleManagerBuilder);
        applyAnnotatedJsonDeserializers(testMethod, persistentSampleManagerBuilder);
    }

    private static Method getDefineSamplersMethod(final SamplerFixture samplerFixture) {
        try {
            return samplerFixture.getClass().getMethod("defineSamplers");
        } catch (final NoSuchMethodException e) {
            // This would be an internal error since the requested method is part of the interface SamplerFixture.
            throw new BaseException("The SamplerFixture %s is missing a method.", e, samplerFixture.getClass().getName());
        }
    }

    /**
     * Activates {@link UseJsonSerializers} from testMethod and it's declaring class. If both are annotated, both are activated.
     * If testMethod declares {@link JsonSerializer}s for the same types as it's declaring class, the ones from testMethod override the ones from the class.
     *
     * @param testMethod
     * @param persistentSampleManagerBuilder
     */
    private static void applyAnnotatedJsonSerializers(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final UseJsonSerializer[] serializersOnMethod = testMethod.getAnnotationsByType(UseJsonSerializer.class);
        final UseJsonSerializer[] serializersOnClass = testMethod.getDeclaringClass().getAnnotationsByType(UseJsonSerializer.class);

        Stream.of(serializersOnClass, serializersOnMethod)
                .flatMap(Stream::of)
                .forEach(serializerAnnotation -> addSerializer(serializerAnnotation, persistentSampleManagerBuilder));
    }

    /**
     * Activates {@link UseJsonDeserializer}s from testMethod and it's declaring class. If both are annotated, both are activated.
     * If testMethod declares {@link JsonDeserializer}s for the same types as it's declaring class, the ones from testMethod override the ones from the class.
     *
     * @param testMethod
     * @param persistentSampleManagerBuilder
     */
    private static void applyAnnotatedJsonDeserializers(final Method testMethod, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final UseJsonDeserializer[] deserializersOnMethod = testMethod.getAnnotationsByType(UseJsonDeserializer.class);
        final UseJsonDeserializer[] deserializersOnClass = testMethod.getDeclaringClass().getAnnotationsByType(UseJsonDeserializer.class);

        Stream.of(deserializersOnClass, deserializersOnMethod)
                .flatMap(Stream::of)
                .forEach(deserializerAnnotation -> addDeserializer(deserializerAnnotation, persistentSampleManagerBuilder));
    }

    @SuppressWarnings({"java:S3740", "unchecked", "rawtypes"})
    // The raw use of parameterized class JsonSerializer is unavoidable here, because the serializerClass is coming
    // from an annotation where no generics other than wildcards are allowed. Since addDeserializer() expects that serializerClass and typeToSerialize have
    // the same type T, we cannot use any generics here - unfortunately.
    private static void addSerializer(final UseJsonSerializer useJsonSerializer, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final Class<? extends JsonSerializer<?>> serializerClass = useJsonSerializer.serializer();
        final Class<?> typeToSerialize = useJsonSerializer.forType();

        validateTypesOfSerializerAndSerializable(serializerClass, typeToSerialize);

        final JsonSerializer jsonSerializer = instantiate(serializerClass);

        persistentSampleManagerBuilder.addSerializer(typeToSerialize, jsonSerializer);
    }

    /**
     * Checks if the generic type of the serializer is the same as the type of the class that should be serialized by the serializer. Usually this would be
     * ensured by generics at compile time, but annotations don't allow the usage of generics other than wildcards.
     *
     * @param serializerClass the type of the {@link JsonSerializer}
     * @param typeToSerialize the type of the classes that should be serialized by serializerClass
     */
    private static void validateTypesOfSerializerAndSerializable(final Class<? extends JsonSerializer<?>> serializerClass, final Class<?> typeToSerialize) {
        final Type[] typeArguments = getParameterizedParentType(serializerClass).getActualTypeArguments();

        if (!typeArguments[0].equals(typeToSerialize)) {
            throw new InvalidConfigException("%s must have a parameter type of type %s since the serializer is registered for the latter type. But it is %s",
                    serializerClass.getName(),
                    typeToSerialize.getName(),
                    typeArguments[0].getTypeName());
        }
    }

    @SuppressWarnings({"java:S3740", "unchecked", "rawtypes"})
    // The raw use of parameterized class JsonSerializer is unavoidable here, because the serializerClass is coming
    // from an annotation where no generics other than wildcards are allowed. Since addDeserializer() expects that serializerClass and typeToSerialize have
    // the same type T, we cannot use any generics here - unfortunately.
    private static void addDeserializer(final UseJsonDeserializer useJsonDeserializer, final JsonSourceManager.Builder persistentSampleManagerBuilder) {
        final Class<? extends JsonDeserializer<?>> deserializerClass = useJsonDeserializer.deserializer();
        final Class<?> typeToDeserialize = useJsonDeserializer.forType();

        validateTypesOfDeserializerAndDeserializable(deserializerClass, typeToDeserialize);

        final JsonDeserializer jsonDeserializer = instantiate(deserializerClass);

        persistentSampleManagerBuilder.addDeserializer(typeToDeserialize, jsonDeserializer);
    }

    /**
     * Checks if the generic type of the serializer is the same as the type of the class that should be serialized by the serializer. Usually this would be
     * ensured by generics at compile time, but annotations don't allow the usage of generics other than wildcards.
     *
     * @param deserializerClass the type of the {@link JsonDeserializer}
     * @param typeToDeserialize the type of the classes that should be deserialized by deserializerClass
     */
    private static void validateTypesOfDeserializerAndDeserializable(final Class<? extends JsonDeserializer<?>> deserializerClass, final Class<?> typeToDeserialize) {
        final Type[] typeArguments = getParameterizedParentType(deserializerClass).getActualTypeArguments();

        if (!typeArguments[0].equals(typeToDeserialize)) {
            throw new InvalidConfigException("%s must have a parameter type of type %s since the deserializer is registered for the latter type. But it is %s",
                    deserializerClass.getName(),
                    typeToDeserialize.getName(),
                    typeArguments[0].getTypeName());
        }
    }

    /**
     * If clazz is a subclass that does not declare a generic type parameter on its own and relies on the type parameter of a
     * parent class, we have to search the type hierarchy for the first parameterized type.
     *
     * @param clazz the class for which we want to find the first parameterized superclass.
     * @return the first parameterized superclass
     */
    private static ParameterizedType getParameterizedParentType(final Class<?> clazz) {
        if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
            return (ParameterizedType) clazz.getGenericSuperclass();
        }

        return getParameterizedParentType((Class<?>) clazz.getGenericSuperclass());
    }

    /**
     * Instantiates clazz using a default constructor. If the class is a inner class, the top declaring outer class is
     * recursively searched and used to initialize the inner class.
     *
     * @param clazz the class that should be instantiated. Inner classes are allowed.
     * @param <T>   The type of the class and the returned instance.
     * @return the new instance of clazz.
     */
    private static <T> T instantiate(final Class<T> clazz) {
        try {
            if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
                Object declaringObject = instantiate(clazz.getDeclaringClass());

                final Constructor<T> constructor = clazz.getDeclaredConstructor(clazz.getDeclaringClass());
                return constructor.newInstance(declaringObject);
            }

            final Constructor<T> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JUnitPreparationException("%s could not be instantiated.", e, clazz.getName());
        }
    }


    private static void applyAnnotatedBeanConverterExtension(final Method testMethod, final PersistentSampleManager persistentSampleManager) {
        final UseBeanConverterExtension useBeanConverterExtensionOnMethod = testMethod.getAnnotation(UseBeanConverterExtension.class);
        final UseBeanConverterExtension useBeanConverterExtensionOnClass = testMethod.getDeclaringClass().getAnnotation(UseBeanConverterExtension.class);

        Stream.of(useBeanConverterExtensionOnMethod, useBeanConverterExtensionOnClass)
                .filter(Objects::nonNull)
                .flatMap(annotation -> Stream.of(annotation.value()))
                .forEach(extensionClass -> addBeanExtension(extensionClass, persistentSampleManager));
    }

    private static void addBeanExtension(final Class<? extends BeanConverterExtension> beanConverterExtensionClass, final PersistentSampleManager persistentSampleManager) {
        BeanConverterExtension extension = instantiate(beanConverterExtensionClass);
        persistentSampleManager.addBeanExtension(extension);
    }

}
