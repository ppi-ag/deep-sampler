/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;


import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.model.PersistentBean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * <p>
 * Interface for extensions of the {@link PersistentBeanConverter}.
 * The PersistentBeanFactory is responsible for creating a generic data-structure for
 * java beans. Also it will convert back this data-structures to the original objects.
 * The reason for this is to omit the type information when serializing objects.
 * </p>
 * <p>
 * As this is a highly generic and complicated process DeepSampler offers extensions
 * for this BeanFactory the {@link BeanConverterExtension}. To add an extension you have
 * to specify it when recording/loading samples with {@link de.ppi.deepsampler.persistence.api.PersistentSampler}
 * ({@link de.ppi.deepsampler.persistence.api.PersistentSampleManager#addBeanExtension(BeanConverterExtension)}.
 * </p>
 * <p>
 * The extension will be used for all processed objects (including embedded objects).
 * </p>
 * <p>
 * When you write an extension you firstly have to define for which type you
 * want to extend the behavior of the original BeanFactory. For this purpose you
 * can use {@link #isProcessable(Class, ParameterizedType)}.
 * </p>
 * <p>
 * After this, every bean with the specified type will be processed within the extension. Now
 * you can implement custom conversion logic yourBean -> generic data-structure (
 * {@link #convert(Object, ParameterizedType, PersistentBeanConverter)}
 * and generic data-structure -> yourBean ({@link #revert(Object, Class, ParameterizedType, PersistentBeanConverter)}).
 * </p>
 * <p>
 * It is also possible to skip the processing of all types for which
 * your implementation of {@link #isProcessable(Class, ParameterizedType)} will return true. This is done by implementing
 * {@link #skip(Class, ParameterizedType)} So you can exclude some types from being processed by the {@link PersistentBeanConverter}.
 * </p>
 */
public interface BeanConverterExtension {

    /**
     * Checks it this extension is responsible for objects of the type beanType.
     *
     * @param beanClass the {@link Class} of the type that will be processed by this extension.
     * @param beanType  the {@link ParameterizedType} of the type that will be processed by this extension. This parameter can only be supplied
     *                  if the type is actually a generic type. If this is not the case, beanType is null.
     * @return true if the {@link Type} should be processed within this extension, false otherwise
     */

    boolean isProcessable(Class<?> beanClass, ParameterizedType beanType);

    /**
     * Skip the conversion of all beans of the given type. Skipped objects will be sent directly to the underlying
     * persistence api. Therefore the persistence api must be able to handle the serialization / deserialization.
     *
     * @param beanClass the {@link Class}of the type that will be skipped by this extension.
     * @param beanType  the {@link ParameterizedType} that will be skipped by this extension. This parameter can only be supplied
     *                  if the type is actually a generic type. If this is not the case, beanType is null.
     * @return true if the Type should be skipped
     */
    boolean skip(Class<?> beanClass, ParameterizedType beanType);

    /**
     * Conversion logic for the type you defined to process and not to skip.
     * Converts an original bean to the {@link PersistentBean} which will be sent to the persistence api.
     * <p>
     * It is also possible to convert bean to any other data structure if the underlying persistence api is fully capable of
     * handling the data structure on its own.
     *
     * @param originalBean            the original bean that is supposed to be converted to a serializable data structure, most likely a {@link PersistentBean}.
     * @param beanType                the {@link ParameterizedType} that will be used for the conversion. This parameter can only be supplied
     *                                if the type is actually a generic type. If this is not the case, beanType is null.
     * @param persistentBeanConverter the current {@link PersistentBeanConverter} that may be used to convert sub objects of bean.
     * @return the generic data-structure for the bean
     */
    Object convert(Object originalBean, ParameterizedType beanType, PersistentBeanConverter persistentBeanConverter);

    /**
     * Conversion logic for the generic data-structure to the processed bean type.
     * Reverts to the original Bean by converting the {@link PersistentBean}, which was deserialized by the underlying
     * persistence api, to the original object.
     * <p>
     * It is also possible to deserialize other types then {@link PersistentBean} if the underlying persistence api is fully
     * capable of deserializing this type. I.E. {@link java.util.List}s can be deserialized by Jackson (the default JSON persistence api)
     * but the elements of the list might be generic {@link PersistentBean}s. The {@link CollectionExtension} would leave it to
     * Jackson to deserialize the List, but it would iterate over that List to revert the {@link PersistentBean}s inside of that list.
     *
     * @param persistentBean          the generic bean
     * @param targetClass             the {@link Class} of the type that will created from the persistentBean.
     * @param targetType              the {@link ParameterizedType}  fo the type that will be created from persistentBean, This parameter can only be supplied
     *                                if the type is actually a generic type. If this is not the case, beanType is null.
     * @param persistentBeanConverter the current {@link PersistentBeanConverter} that may be used to revert sub objects of persistentBean.
     * @param <T>                     type of the original bean
     * @return original bean
     */
    <T> T revert(Object persistentBean, Class<T> targetClass, ParameterizedType targetType, PersistentBeanConverter persistentBeanConverter);
}
