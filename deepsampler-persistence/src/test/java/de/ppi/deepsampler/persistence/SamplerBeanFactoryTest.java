package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.model.PersistentBean;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SamplerBeanFactoryTest {

    @Test
    void testImmutableCollectionBean() {
        // GIVEN
        final CollectionBean bean = new CollectionBean();
        List<String> listOfStrings = new ArrayList<>();
        listOfStrings.add("AB");
        listOfStrings.add("CD");
        bean.collectionOfStrings = Collections.unmodifiableList(listOfStrings);

        // WHEN
        PersistentBean persistentBean = SamplerBeanFactory.create().toBean(bean, bean.getClass());

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
        PersistentBean persistentBean = SamplerBeanFactory.create().toBean(timestampBean, timestampBean.getClass());

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