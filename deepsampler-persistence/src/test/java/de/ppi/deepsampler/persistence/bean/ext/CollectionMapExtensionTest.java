package de.ppi.deepsampler.persistence.bean.ext;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CollectionMapExtensionTest {

    @Test
    void testArrayListProcessableAndSkipped() {
        // GIVEN
        BeanConverterExtension extension = new CollectionMapExtension();
        final Class<ArrayList> beanCls = ArrayList.class;

        // WHEN
        boolean isProcessable = extension.isProcessable(beanCls);
        boolean isSkip = extension.skip(beanCls);

        // THEN
        assertTrue(isProcessable);
        assertTrue(isSkip);
    }

    @Test
    void testHashSetProcessableAndSkipped() {
        // GIVEN
        BeanConverterExtension extension = new CollectionMapExtension();
        final Class<HashSet> beanCls = HashSet.class;

        // WHEN
        boolean isProcessable = extension.isProcessable(beanCls);
        boolean isSkip = extension.skip(beanCls);

        // THEN
        assertTrue(isProcessable);
        assertTrue(isSkip);
    }

    @Test
    void testStringProcessableAndSkipped() {
        // GIVEN
        BeanConverterExtension extension = new CollectionMapExtension();
        final Class<String> beanCls = String.class;

        // WHEN
        boolean isProcessable = extension.isProcessable(beanCls);
        boolean isSkip = extension.skip(beanCls);

        // THEN
        assertFalse(isProcessable);
        assertFalse(isSkip);
    }

    @Test
    void testMapProcessableAndSkipped() {
        // GIVEN
        BeanConverterExtension extension = new CollectionMapExtension();
        final Class<HashMap> beanCls = HashMap.class;

        // WHEN
        boolean isProcessable = extension.isProcessable(beanCls);
        boolean isSkip = extension.skip(beanCls);

        // THEN
        assertTrue(isProcessable);
        assertTrue(isSkip);
    }
}