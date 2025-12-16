package com.github.biiiiiigmonster.metamodel;

import java.util.Objects;

/**
 * Represents a single-valued persistent attribute in the static metamodel.
 * <p>
 * This class provides type-safe field references for Model entities, enabling
 * compile-time safety and IDE auto-completion support when referencing entity fields
 * in relation definitions and queries.
 *
 * @param <E> The entity type that declares this attribute
 * @param <T> The type of the attribute
 */
public class SingularAttribute<E, T> {
    private final String name;
    private final Class<E> declaringType;
    private final Class<T> javaType;

    /**
     * Creates a new SingularAttribute with the specified metadata.
     *
     * @param name          the field name as it appears in the entity class
     * @param declaringType the entity class that declares this attribute
     * @param javaType      the Java type of this attribute
     */
    public SingularAttribute(String name, Class<E> declaringType, Class<T> javaType) {
        this.name = name;
        this.declaringType = declaringType;
        this.javaType = javaType;
    }

    /**
     * Returns the field name as a string for use in queries and relations.
     *
     * @return the field name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the entity class that declares this attribute.
     *
     * @return the declaring entity class
     */
    public Class<E> getDeclaringType() {
        return declaringType;
    }

    /**
     * Returns the Java type of this attribute.
     *
     * @return the attribute's Java type class
     */
    public Class<T> getJavaType() {
        return javaType;
    }

    /**
     * Returns the field name - enables implicit string conversion.
     * <p>
     * This allows the attribute to be used directly where a String field name
     * is expected, providing seamless integration with existing APIs.
     *
     * @return the field name
     */
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingularAttribute<?, ?> that = (SingularAttribute<?, ?>) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(declaringType, that.declaringType) &&
               Objects.equals(javaType, that.javaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, declaringType, javaType);
    }
}
