/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;


import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.model.PersistentBean;

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
 * can use {@link #isProcessable(Class)}.
 * </p>
 * <p>
 * After this every bean with the specified type will be processed within the extension. Now
 * you can implement custom conversion logic yourBean -> generic data-structure (
 * {@link #convert(Object)} and generic data-structure -> yourBean ({@link #revert(PersistentBean, Class)}).
 * </p>
 * <p>
 * Besides this you can also make the BeanFactory skip the processing of all types for which
 * your implementation {@link #isProcessable(Class)} will return true. So you can exclude
 * some types from being processed by the factory.
 * </p>
 */
public interface BeanConverterExtension {

    /**
     * Describe the target types you want to process withing this extension.
     *
     * @param beanCls the class of the bean for which it will be
     * @return true when the class should be processed within this extension, false otherwise
     */
    boolean isProcessable(Class<?> beanCls);

    /**
     * Skip the conversion of all beans of the given type.
     *
     * @param beanCls the type you might want to skip
     * @return true if the class should get skipped
     */
    boolean skip(Class<?> beanCls);

    /**
     * Conversion logic for the type you defined to process and not to skip.
     * Converts an original bean to the {@link PersistentBean} which will be sent to the persistence api.
     *
     * @param bean the bean
     * @return the generic data-structure for the bean
     */
    PersistentBean convert(Object bean);

    /**
     * Conversion logic for the generic data-structure to the processed bean type.
     * Reverts to the original Bean by converting the {@link PersistentBean}, which was deserialized by the underlying
     * persitence api , to the original object.
     *
     * @param bean the generic bean
     * @param target the target class
     * @param <T> type of the original bean
     * @return original bean
     */
    <T> T revert(PersistentBean bean, Class<T> target);
}
