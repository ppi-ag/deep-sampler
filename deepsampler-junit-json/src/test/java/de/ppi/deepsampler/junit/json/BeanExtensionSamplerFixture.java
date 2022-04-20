/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit.json;

import de.ppi.deepsampler.core.api.PersistentSample;
import de.ppi.deepsampler.junit.Cat;
import de.ppi.deepsampler.junit.PrepareSampler;
import de.ppi.deepsampler.junit.SamplerFixture;
import de.ppi.deepsampler.junit.TestService;
import de.ppi.deepsampler.junit.UseBeanConverterExtension;
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
        public boolean isProcessable(final Class<?> beanClass, final ParameterizedType beanType) {
            return beanClass.equals(Cat.class);
        }

        @Override
        public boolean skip(final Class<?> beanClass, final ParameterizedType beanType) {
            return false;
        }

        @Override
        public Object convert(final Object originalBean, final ParameterizedType beanType, final PersistentBeanConverter persistentBeanConverter) {
            return ((Cat) originalBean).getName();
        }

        @Override
        public <T> T revert(final Object persistentBean, final Class<T> targetClass, final ParameterizedType targetType, final PersistentBeanConverter persistentBeanConverter) {
            final String catsName = (String) persistentBean;
            return (T) new Cat(catsName);
        }
    }
}