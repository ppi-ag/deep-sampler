package de.ppi.deepsampler.persistence;

import de.ppi.deepsampler.persistence.bean.PersistentBeanFactory;
import de.ppi.deepsampler.persistence.model.PersistentBean;
import org.junit.jupiter.api.Test;

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
        PersistentBean persistentBean = SamplerBeanFactory.create().toBean(bean);

        // THEN
        assertEquals(bean.collectionOfStrings, persistentBean.getValue("0$collectionOfStrings"));
    }

    private static class CollectionBean {
        Collection<String> collectionOfStrings;
    }

}