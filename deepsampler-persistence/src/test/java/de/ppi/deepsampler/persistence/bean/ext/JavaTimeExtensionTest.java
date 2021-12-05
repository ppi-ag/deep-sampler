/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.bean.ext;

import de.ppi.deepsampler.persistence.bean.PersistentBeanConverter;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JavaTimeExtensionTest {

    @Test
    void shouldAcceptAllJavaTimeTypes() {
        // GIVEN
        Class<Date> dateClass = Date.class;
        Class<LocalDateTime> localDateTimeClass = LocalDateTime.class;
        Class<LocalDate> localDateClass = LocalDate.class;
        Class<java.sql.Date> sqlDateClass = java.sql.Date.class;
        Class<Timestamp> sqlTimeStampClass = Timestamp.class;

        JavaTimeExtension extension = new JavaTimeExtension();

        // THEN
        assertTrue(extension.isProcessable(dateClass, null));
        assertTrue(extension.isProcessable(localDateTimeClass, null));
        assertTrue(extension.isProcessable(localDateClass, null));
        assertTrue(extension.isProcessable(sqlDateClass, null));
        assertTrue(extension.isProcessable(sqlTimeStampClass, null));

        assertFalse(extension.isProcessable(String.class, null));
    }

    @Test
    void shouldConvertJavaTimeTypes() {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new JavaTimeExtension());

        Date date = new Date();

        Date result = converter.convert(date, null);
        assertSame(date, result);
    }

    @Test
    void shouldRevertJavaTimeTypes() {
        // GIVEN
        PersistentBeanConverter converter = new PersistentBeanConverter();
        converter.addExtension(new JavaTimeExtension());

        Date date = new Date();

        Date result = converter.revert(date, Date.class, null);
        assertSame(date, result);
    }

}