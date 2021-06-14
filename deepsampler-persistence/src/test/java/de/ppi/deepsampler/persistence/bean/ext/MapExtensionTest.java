package de.ppi.deepsampler.persistence.bean.ext;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MapExtensionTest {

    @Test
    void testStringProcessableAndSkipped() {
        // GIVEN
        BeanConverterExtension extension = new MapExtension();
        final Class<String> beanCls = String.class;

        // WHEN
        boolean isProcessable = extension.isProcessable(beanCls, null);
        boolean isSkip = extension.skip(beanCls, null);

        // THEN
        assertFalse(isProcessable);
        assertFalse(isSkip);
    }

    @Test
    void testMapProcessableAndSkipped() {
        // GIVEN
        BeanConverterExtension extension = new MapExtension();
        final Class<HashMap> beanCls = HashMap.class;

        // WHEN
        boolean isProcessable = extension.isProcessable(beanCls, null);
        boolean isSkip = extension.skip(beanCls, null);

        // THEN
        assertTrue(isProcessable);
        assertTrue(isSkip);
    }
}