/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;


import com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi;
import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.model.PersistentBean;

import java.lang.reflect.Type;

/**
 * <p>
 * Interface for extensions of the {@link PersistentBeanConverter}.
 * The PersistentBeanFactory is responsible for creating a generic data-structure for
 * java beans. Also it will convert back this data-structures to the original objects.
 * The reason for this is to omit the type information when serializing objects.
 * </p>
 *
 * <br>
 *
 * <p>
 * As this is a highly generic and complicated process deepsampler offers extensions
 * for this BeanFactory the {@link BeanConverterExtension}. To add an extension you have
 * to specifiy it when recording/loading samples with {@link de.ppi.deepsampler.persistence.api.PersistentSampler}
 * ({@link de.ppi.deepsampler.persistence.api.PersistentSampleManager#addBeanExtension(BeanConverterExtension)}.
 * </p>
 * <br>
 * <p>
 * The extension will be used for all processed objects (including embedded objects).
 * </p>
 * <br>
 * <p>
 * When you write an extension you firstly have to define which for which typed you
 * want to extend the behavior of the original BeanFactory. For this purpose you
 * can use {@link #isProcessable(Type)}.
 * </p>
 * <p>
 * After this every bean with the specified type will be processed within the extension. Now
 * you can implement custom conversion logic yourBean -> generic data-structure (
 * {@link #convert(Object, PersistentBeanConverter)} and generic data-structure -> yourBean ({@link #revert(Object, Type, PersistentBeanConverter)}).
 * </p>
 * <p>
 * Besides this you can also make the BeanFactory skip the processing of all types for which
 * your implementation {@link #isProcessable(Type)} will return true. So you can exclude
 * some types from being processed by the factory.
 * </p>
 */
public interface BeanConverterExtension {

    /**
     * Checks it this extension is responsible for objects of the type beanType.
     *
     * @param beanType the {@link Type} which might be converted by this extension. beanType could be an ordinary {@link Class} or
     *                 a {@link java.lang.reflect.ParameterizedType} for generic Types.
     * @return true if the {@link Type} should be processed within this extension, false otherwise
     */
    boolean isProcessable(Type beanType);

    /**
     * Skip the conversion of all beans of the given type. Skipped objects will be sent directly to the underlying
     * persistence api. Therefore the persistence api must be able to handle the serialization / deserialization.
     *
     * @param beanType the type you might want to skip
     * @return true if the Type should be skipped
     */
    boolean skip(Type beanType);

    /**
     * Conversion logic for the type you defined to process and not to skip.
     * Converts an original bean to the {@link PersistentBean} which will be sent to the persistence api.
     *
     * It is also possible to convert bean to any other data structure if the underlying persistence api is fully capable of
     * handling the data structure on its own.
     *
     * @param originalBean the original bean that is supposed to be converted to a serializable data structure, most likely a {@link PersistentBean}.
     * @param persistentBeanConverter the current {@link PersistentBeanConverter} that may be used to convert sub objects of bean.
     * @return the generic data-structure for the bean
     */
    Object convert(Object originalBean, PersistentBeanConverter persistentBeanConverter);

    /**
     * Conversion logic for the generic data-structure to the processed bean type.
     * Reverts to the original Bean by converting the {@link PersistentBean}, which was deserialized by the underlying
     * persistence api, to the original object.
     *
     * It is also possible to deserialize other types then {@link PersistentBean} if the underlying persistence api is fully
     * capable of deserializing this type. I.E. {@link java.util.List}s can be deserialized by Jackson (the default JSON persistence api)
     * but the elements of the list might be generic {@link PersistentBean}s. The {@link CollectionExtension} would leave it to
     * Jackson to deserialize the List, but it would iterate over that List to revert the {@link PersistentBean}s inside of that list.
     *
     * @param persistentBean the generic bean
     * @param target the target class
     * @param persistentBeanConverter the current {@link PersistentBeanConverter} that may be used to revert sub objects of persistentBean.
     * @param <T> type of the original bean
     * @return original bean
     */
    <T> T revert(Object persistentBean, Type target, PersistentBeanConverter persistentBeanConverter);
}
