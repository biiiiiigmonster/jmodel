package com.github.biiiiiigmonster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.biiiiiigmonster.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * Sample entity for testing metamodel generation.
 * <p>
 * This entity includes various field types to verify the metamodel generator
 * correctly handles:
 * - Primitive types (int, long, boolean, double)
 * - Wrapper types (Long, Integer, String, Boolean, Double)
 * - Date/time types (Date, LocalDateTime, LocalDate, LocalTime)
 * - Enum types (Status)
 * - Custom object types (BigDecimal)
 * - Non-persistent fields marked with @TableField(exist=false)
 * <p>
 * Requirements: 1.1, 1.3, 1.4, 5.1, 5.2, 5.3, 5.4, 5.5
 */
@Data
@TableName(excludeProperty = "pivot")("sample_entity")
@EqualsAndHashCode(callSuper = false)
public class SampleEntity extends Model<SampleEntity> {

    /**
     * Status enum for testing enum type field generation.
     */
    public enum Status {
        ACTIVE,
        INACTIVE,
        PENDING,
        DELETED
    }

    // Primary key
    @TableId
    private Long id;

    // Primitive types (Requirements 5.1)
    private int primitiveInt;
    private long primitiveLong;
    private boolean primitiveBoolean;
    private double primitiveDouble;

    // Wrapper types (Requirements 5.2)
    private Integer wrapperInteger;
    private Long wrapperLong;
    private String stringField;
    private Boolean wrapperBoolean;
    private Double wrapperDouble;

    // Date/time types (Requirements 5.3)
    private Date dateField;
    private LocalDateTime localDateTimeField;
    private LocalDate localDateField;
    private LocalTime localTimeField;

    // Enum type (Requirements 5.4)
    private Status status;

    // Custom object type (Requirements 5.5)
    private BigDecimal amount;

    // Non-persistent fields - should be EXCLUDED from metamodel (Requirements 1.4)
    @TableField(exist = false)
    private String transientField;

    @TableField(exist = false)
    private User relatedUser;

    @TableField(exist = false)
    private Object computedValue;
}
