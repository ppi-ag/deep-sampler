/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.api.PersistentSample;
import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import de.ppi.deepsampler.persistence.bean.ext.BeanConverterExtension;

import java.lang.reflect.ParameterizedType;

/**
 * A simple {@link SamplerFixture} that is used to test the annotation {@link UseBeanConverterExtension}
 */
@UseBeanConverterExtension(BeanExtensionSamplerFixture.CatBeanExtension.class)
public class BeanExtensionSamplerFixture implements SamplerFixture {


    @PrepareSampler
    private TestService testServiceSampler;

    @Override
    public void defineSamplers() {
        PersistentSample.of(testServiceSampler.getCat()).hasId("CatStub");
        PersistentSample.of(testServiceSampler.getOptionalCatsName()).hasId("getOptionalCatsName");
        PersistentSample.of(testServiceSampler.getOptionalCat()).hasId("getOptionalCat");
        PersistentSample.of(testServiceSampler.getOptionalGenericCat()).hasId("getOptionalGenericCat");
    }


    public static class CatBeanExtension implements BeanConverterExtension {

        @Override
        public boolean isProcessable(Class<?> beanClass, ParameterizedType beanType) {
            return beanClass.equals(Cat.class);
        }

        @Override
        public boolean skip(Class<?> beanClass, ParameterizedType beanType) {
            return false;
        }

        @Override
        public Object convert(Object originalBean, ParameterizedType beanType, PersistentBeanConverter persistentBeanConverter) {
            return ((Cat) originalBean).getName();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T revert(Object persistentBean, Class<T> targetClass, ParameterizedType targetType, PersistentBeanConverter persistentBeanConverter) {
            String catsName = (String) persistentBean;
            return (T) new Cat(catsName);
        }
    }
}