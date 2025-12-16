package com.github.biiiiiigmonster.processor;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SingularAttribute class.
 * <p>
 * These tests verify that SingularAttribute correctly stores and retrieves
 * field metadata including name, declaring type, and java type.
 * <p>
 * Requirements: 2.1, 2.2, 2.3, 2.4, 5.1, 5.2, 5.3, 5.4, 5.5
 */
class SingularAttributeTest {

    /**
     * Test that SingularAttribute correctly stores and returns field name.
     * Requirements: 2.1, 2.4
     */
    @Test
    void fieldNameRoundTrip() {
        SingularAttribute<Object, String> attr = new SingularAttribute<>("testField", Object.class, String.class);
        
        assertEquals("testField", attr.getName());
        assertEquals("testField", attr.toString());
    }

    /**
     * Test that SingularAttribute preserves type information for various types.
     * Requirements: 2.2, 5.1, 5.2, 5.3, 5.4, 5.5
     */
    @Test
    void typeInformationPreservation() {
        // Test with various types
        SingularAttribute<Object, Long> longAttr = new SingularAttribute<>("id", Object.class, Long.class);
        assertEquals(Long.class, longAttr.getJavaType());

        SingularAttribute<Object, String> stringAttr = new SingularAttribute<>("name", Object.class, String.class);
        assertEquals(String.class, stringAttr.getJavaType());

        SingularAttribute<Object, Integer> intAttr = new SingularAttribute<>("count", Object.class, Integer.class);
        assertEquals(Integer.class, intAttr.getJavaType());
    }

    /**
     * Test that SingularAttribute preserves declaring type.
     * Requirements: 2.3
     */
    @Test
    void declaringTypePreservation() {
        class TestEntity {}
        
        SingularAttribute<TestEntity, String> attr = new SingularAttribute<>("field", TestEntity.class, String.class);
        assertEquals(TestEntity.class, attr.getDeclaringType());
    }


    /**
     * Test SingularAttribute equality and hashCode.
     */
    @Test
    void equalityAndHashCode() {
        SingularAttribute<Object, String> attr1 = new SingularAttribute<>("field", Object.class, String.class);
        SingularAttribute<Object, String> attr2 = new SingularAttribute<>("field", Object.class, String.class);
        SingularAttribute<Object, String> attr3 = new SingularAttribute<>("other", Object.class, String.class);

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertNotEquals(attr1, attr3);
    }

    /**
     * Test that primitive types are correctly mapped to wrapper types.
     * Requirements: 5.1
     */
    @Test
    void primitiveTypeMappingToWrappers() {
        // Verify wrapper types work correctly with SingularAttribute
        SingularAttribute<Object, Integer> intAttr = new SingularAttribute<>("primitiveInt", Object.class, Integer.class);
        assertEquals(Integer.class, intAttr.getJavaType());

        SingularAttribute<Object, Long> longAttr = new SingularAttribute<>("primitiveLong", Object.class, Long.class);
        assertEquals(Long.class, longAttr.getJavaType());

        SingularAttribute<Object, Boolean> boolAttr = new SingularAttribute<>("primitiveBoolean", Object.class, Boolean.class);
        assertEquals(Boolean.class, boolAttr.getJavaType());

        SingularAttribute<Object, Double> doubleAttr = new SingularAttribute<>("primitiveDouble", Object.class, Double.class);
        assertEquals(Double.class, doubleAttr.getJavaType());
    }

    /**
     * Test that wrapper types are correctly handled.
     * Requirements: 5.2
     */
    @Test
    void wrapperTypeHandling() {
        SingularAttribute<Object, Integer> integerAttr = new SingularAttribute<>("wrapperInteger", Object.class, Integer.class);
        SingularAttribute<Object, Long> longAttr = new SingularAttribute<>("wrapperLong", Object.class, Long.class);
        SingularAttribute<Object, String> stringAttr = new SingularAttribute<>("stringField", Object.class, String.class);
        SingularAttribute<Object, Boolean> booleanAttr = new SingularAttribute<>("wrapperBoolean", Object.class, Boolean.class);
        SingularAttribute<Object, Double> doubleAttr = new SingularAttribute<>("wrapperDouble", Object.class, Double.class);

        assertEquals(Integer.class, integerAttr.getJavaType());
        assertEquals(Long.class, longAttr.getJavaType());
        assertEquals(String.class, stringAttr.getJavaType());
        assertEquals(Boolean.class, booleanAttr.getJavaType());
        assertEquals(Double.class, doubleAttr.getJavaType());
    }

    /**
     * Test that date/time types are correctly handled.
     * Requirements: 5.3
     */
    @Test
    void dateTimeTypeHandling() {
        SingularAttribute<Object, Date> dateAttr = 
            new SingularAttribute<>("dateField", Object.class, Date.class);
        SingularAttribute<Object, LocalDateTime> localDateTimeAttr = 
            new SingularAttribute<>("localDateTimeField", Object.class, LocalDateTime.class);
        SingularAttribute<Object, LocalDate> localDateAttr = 
            new SingularAttribute<>("localDateField", Object.class, LocalDate.class);
        SingularAttribute<Object, LocalTime> localTimeAttr = 
            new SingularAttribute<>("localTimeField", Object.class, LocalTime.class);

        assertEquals(Date.class, dateAttr.getJavaType());
        assertEquals(LocalDateTime.class, localDateTimeAttr.getJavaType());
        assertEquals(LocalDate.class, localDateAttr.getJavaType());
        assertEquals(LocalTime.class, localTimeAttr.getJavaType());
    }

    /**
     * Test enum for enum type handling test.
     */
    enum TestStatus { ACTIVE, INACTIVE }

    /**
     * Test that enum types are correctly handled.
     * Requirements: 5.4
     */
    @Test
    void enumTypeHandling() {
        SingularAttribute<Object, TestStatus> statusAttr = 
            new SingularAttribute<>("status", Object.class, TestStatus.class);
        
        assertEquals(TestStatus.class, statusAttr.getJavaType());
        assertTrue(statusAttr.getJavaType().isEnum());
    }

    /**
     * Test that custom object types are correctly handled.
     * Requirements: 5.5
     */
    @Test
    void customObjectTypeHandling() {
        SingularAttribute<Object, BigDecimal> amountAttr = 
            new SingularAttribute<>("amount", Object.class, BigDecimal.class);
        
        assertEquals(BigDecimal.class, amountAttr.getJavaType());
    }
}
