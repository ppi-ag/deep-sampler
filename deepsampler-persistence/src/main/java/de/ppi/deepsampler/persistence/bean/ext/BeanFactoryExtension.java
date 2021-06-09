/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;


import de.ppi.deepsampler.persistence.model.PersistentBean;

import java.lang.reflect.Type;

/**
 * <p>
 * Interface for extensions of the {@link de.ppi.deepsampler.persistence.bean.PersistentBeanFactory}.
 * The PersistentBeanFactory is responsible for creating a generic data-structure for
 * java beans. Also it will convert back this data-structures to the original objects.
 * This approach enables us to omit the type information when serializing objects.
 * </p>
 *
 * <br>
 *
 * <p>
 * As this is a highly generic and complicated process deepsampler offers extensions
 * for this BeanFactory the {@link BeanFactoryExtension}. To add an extension you have
 * to specify it when recording/loading samples with {@link de.ppi.deepsampler.persistence.api.PersistentSampler}
 * ({@link de.ppi.deepsampler.persistence.api.PersistentSampleManager#addBeanExtension(BeanFactoryExtension)}.
 * </p>
 * <br>
 * <p>
 * The extension will be used for all processed objects (including embedded objects).
 * </p>
 * <br>
 * <p>
 * When you write an extension you firstly have to define for which type you
 * want to extend the behavior of the original BeanFactory. For this purpose you
 * can use {@link #isProcessable(Type)}.
 * </p>
 * <p>
 * After this every bean with the specified type will be processed within the extension. Now
 * you can implement custom conversion logic yourBean -> generic data-structure (
 * {@link #toBean(Object, Type)} and generic data-structure -> yourBean ({@link #ofBean(Object, Type)}).
 * </p>
 * <p>
 * It is also possible to make the BeanFactory skip the processing of all types for which
 * your implementation {@link #isProcessable(Type)} will return true. So you can exclude
 * some types from being processed by the factory.
 * </p>
 */
public interface BeanFactoryExtension {

    /**
     * Describe the target {@link java.sql.Types} you want to process withing this extension. A target {@link Type} is
     * the {@link Type} of the object that that will be converted to a generic {@link PersistentBean} in order to persist it.
     *
     * @param beanType the {@link Type} of the target bean. If the target bean has a generic Type, beanType will be a {@link java.lang.reflect.ParameterizedType},
     *                 otherwise it will be a {@link Class}.
     * @return true when the beanType should be processed within this extension, false otherwise
     */
    boolean isProcessable(Type beanType);

    /**
     * Skip the conversion of all target beans of the given type to the generic data-structure
     * {@link PersistentBean}.
     *
     * skip() is used only while recording, not while loading data, so {@link BeanFactoryExtension#ofBean(Object, Type)}
     * is never skpped.
     *
     * @param beanType the type you might want to skip
     * @return true if the class should get skipped
     */
    boolean skip(Type beanType);

    /**
     * Conversion logic for the type you defined to process and not to skip.
     *
     * @param targetBean the bean
     * @param targetBeanType the {@link Type} of bean. beanType is a {@link java.lang.reflect.ParameterizedType} if bean is a generic Type.
     *                 This gives access to the actual types of the generic type parameters.
     *                 If bean is not a generic type, beanType is a common {@link Class} corresponding to bean.getClass().
     *
     * @return the generic data-structure for the bean
     */
    <T> T toBean(Object targetBean, Type targetBeanType);

    /**
     * Conversion logic for the generic data-structure to the processed bean type.
     *
     * @param persistentBean the generic bean
     * @param targetBeanType the {@link Type} of the target bean. beanType is a {@link java.lang.reflect.ParameterizedType} if bean is a generic Type.
     *      *                 This gives access to the actual types of the generic type parameters.
     *      *                 If bean is not a generic type, beanType is a common {@link Class} corresponding to bean.getClass().
     * @return original bean
     */
    <T> T ofBean(Object persistentBean, Type targetBeanType);
}
