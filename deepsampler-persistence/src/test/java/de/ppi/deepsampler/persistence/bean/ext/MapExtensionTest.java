package de.ppi.deepsampler.persistence.bean.ext;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class MapExtensionTest {

    @Test
    void testStringProcessableAndSkipped() {
        // GIVEN
        BeanConverterExtension extension = new MapExtension();
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
        BeanConverterExtension extension = new MapExtension();
        final Class<HashMap> beanCls = HashMap.class;

        // WHEN
        boolean isProcessable = extension.isProcessable(beanCls);
        boolean isSkip = extension.skip(beanCls);

        // THEN
        assertTrue(isProcessable);
        assertTrue(isSkip);
    }
}