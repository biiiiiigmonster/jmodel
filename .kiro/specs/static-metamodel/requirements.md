# Requirements Document

## Introduction

This feature implements a Static Metamodel Generator for the jmodel ORM framework, inspired by Hibernate JPA's static metamodel pattern. The generator creates compile-time static attribute classes for entities that extend `Model`, enabling type-safe field references in relation definitions and queries. Instead of using error-prone string literals like `"id"` or `"userId"`, developers can use generated static attributes like `User_.id` or `User_.userId`, providing compile-time safety and IDE auto-completion support.

## Glossary

- **Static Metamodel**: A set of generated classes containing static field references that mirror the attributes of entity classes
- **Model Entity**: A Java class that extends the `Model<T>` base class in the jmodel framework
- **Annotation Processor**: A compile-time tool that processes Java annotations and generates source code
- **SingularAttribute**: A static field reference representing a single-valued persistent attribute
- **Generated_**: The suffix convention for generated metamodel classes (e.g., `User` → `User_`)
- **APT (Annotation Processing Tool)**: Java's mechanism for processing annotations at compile time
- **jmodel-core**: The core module containing Model base class, relation annotations, and ORM utilities
- **jmodel-metamodel**: The metamodel module containing the annotation processor and SingularAttribute class
- **Multi-Module Project**: A Maven project structure where the annotation processor is in a separate module from the entities it processes

## Requirements

### Requirement 1

**User Story:** As a developer, I want metamodel classes to be automatically generated for my Model entities, so that I can reference entity fields in a type-safe manner.

#### Acceptance Criteria

1. WHEN a class extends `Model<T>` and is compiled THEN the Annotation Processor SHALL generate a corresponding metamodel class with the suffix `_` (e.g., `User` → `User_`)
2. WHEN the metamodel class is generated THEN the Annotation Processor SHALL place the generated class in the same package as the source entity
3. WHEN the source entity contains persistent fields THEN the Generated Metamodel SHALL include a public static final field for each persistent attribute
4. WHEN a field is annotated with `@TableField(exist = false)` THEN the Annotation Processor SHALL exclude that field from the generated metamodel
5. WHEN the source entity is modified and recompiled THEN the Annotation Processor SHALL regenerate the metamodel class to reflect the changes

### Requirement 2

**User Story:** As a developer, I want the generated static attributes to contain field metadata, so that I can use them in relation definitions and queries.

#### Acceptance Criteria

1. WHEN a static attribute is generated THEN the SingularAttribute SHALL contain the field name as a string value
2. WHEN a static attribute is generated THEN the SingularAttribute SHALL contain the field type information
3. WHEN a static attribute is generated THEN the SingularAttribute SHALL contain a reference to the declaring entity class
4. WHEN a developer accesses a static attribute THEN the SingularAttribute SHALL provide a method to retrieve the field name string

### Requirement 3

**User Story:** As a developer, I want to use generated static attributes in relation annotation parameters, so that I can define relations with compile-time type safety.

#### Acceptance Criteria

1. WHEN defining a relation with `foreignKey` parameter THEN the Developer SHALL be able to use a static attribute reference instead of a string literal
2. WHEN defining a relation with `localKey` parameter THEN the Developer SHALL be able to use a static attribute reference instead of a string literal
3. WHEN defining a relation with `ownerKey` parameter THEN the Developer SHALL be able to use a static attribute reference instead of a string literal
4. WHEN a static attribute from an incompatible entity is used THEN the Compiler SHALL report a type error

### Requirement 4

**User Story:** As a developer, I want the annotation processor to be easily integrated into my build process, so that metamodel generation happens automatically during compilation.

#### Acceptance Criteria

1. WHEN the jmodel dependency is added to a Maven project THEN the Annotation Processor SHALL be automatically discovered via ServiceLoader mechanism
2. WHEN the project is compiled with Maven THEN the Annotation Processor SHALL generate metamodel classes in the `target/generated-sources/annotations` directory
3. WHEN compilation errors occur in the source entity THEN the Annotation Processor SHALL handle errors gracefully and report meaningful error messages
4. WHEN the annotation processor runs THEN the Annotation Processor SHALL add `@Generated` annotation to generated classes with processor information

### Requirement 7

**User Story:** As a developer, I want the project to be structured as a multi-module Maven project, so that the annotation processor can execute correctly during compilation.

#### Acceptance Criteria

1. WHEN the project is restructured THEN the Build System SHALL create a parent POM with two child modules: jmodel-core and jmodel-metamodel
2. WHEN jmodel-core module is compiled THEN the Module SHALL contain the Model base class, relation annotations, and all existing ORM utilities
3. WHEN jmodel-metamodel module is compiled THEN the Module SHALL contain the annotation processor and SingularAttribute class
4. WHEN jmodel-metamodel depends on jmodel-core THEN the Annotation Processor SHALL be able to detect Model subclasses
5. WHEN test entities in jmodel-core depend on jmodel-metamodel THEN the Annotation Processor SHALL generate metamodel classes for all Model entities during test compilation

### Requirement 5

**User Story:** As a developer, I want the generated metamodel to support all field types used in my entities, so that I can reference any persistent attribute.

#### Acceptance Criteria

1. WHEN an entity contains primitive type fields THEN the Generated Metamodel SHALL include corresponding static attributes with proper type information
2. WHEN an entity contains wrapper type fields (Long, Integer, String, etc.) THEN the Generated Metamodel SHALL include corresponding static attributes
3. WHEN an entity contains date/time type fields THEN the Generated Metamodel SHALL include corresponding static attributes
4. WHEN an entity contains enum type fields THEN the Generated Metamodel SHALL include corresponding static attributes
5. WHEN an entity contains custom object type fields that are persistent THEN the Generated Metamodel SHALL include corresponding static attributes

### Requirement 6

**User Story:** As a developer, I want clear documentation and examples for using the static metamodel feature, so that I can quickly adopt it in my projects.

#### Acceptance Criteria

1. WHEN the feature is released THEN the Documentation SHALL include usage examples showing before/after comparison of string literals vs static attributes
2. WHEN the feature is released THEN the Documentation SHALL include Maven configuration instructions for enabling the annotation processor
3. WHEN the feature is released THEN the Documentation SHALL include troubleshooting guidance for common issues
