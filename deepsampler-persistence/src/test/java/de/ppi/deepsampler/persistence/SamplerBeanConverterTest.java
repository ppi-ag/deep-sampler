/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.model.PersistentBean;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SamplerBeanConverterTest {

    @Test
    void testImmutableCollectionBean() {
        // GIVEN
        final CollectionBean bean = new CollectionBean();
        List<String> listOfStrings = new ArrayList<>();
        listOfStrings.add("AB");
        listOfStrings.add("CD");
        bean.collectionOfStrings = Collections.unmodifiableList(listOfStrings);

        // WHEN
        PersistentBean persistentBean = SamplerBeanConverter.create().convert(bean, null);

        // THEN
        assertEquals(bean.collectionOfStrings, persistentBean.getValue("0$collectionOfStrings"));
    }

    @Test
    void testTimestampSql() {
        // GIVEN
        Timestamp ts =  new Timestamp(1L);
        final TimestampBean timestampBean = new TimestampBean();
        timestampBean.timestamp = ts;

        // WHEN
        PersistentBean persistentBean = SamplerBeanConverter.create().convert(timestampBean, null);

        // THEN
        assertEquals(ts, persistentBean.getValue("0$timestamp"));
    }

    private static class TimestampBean {
        Timestamp timestamp;
    }

    private static class CollectionBean {
        Collection<String> collectionOfStrings;
    }

}