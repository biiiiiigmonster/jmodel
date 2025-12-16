# Implementation Plan

- [ ] 1. Restructure project to multi-module Maven structure
  - [x] 1.1 Create parent POM and module structure





    - Create parent `pom.xml` with modules declaration
    - Move existing `src/` to `jmodel-core/src/`
    - Create `jmodel-core/pom.xml` with core dependencies
    - Create `jmodel-metamodel/` directory structure
    - Create `jmodel-metamodel/pom.xml` with processor dependencies
    - _Requirements: 7.1, 7.2, 7.3_
  - [x] 1.2 Configure module dependencies





    - Add jmodel-core dependency to jmodel-metamodel
    - Add jmodel-metamodel as annotation processor dependency in jmodel-core test scope
    - Configure maven-compiler-plugin for annotation processing in jmodel-core
    - _Requirements: 7.4, 7.5_

- [ ] 2. Create core metamodel infrastructure in jmodel-metamodel
  - [x] 2.1 Create SingularAttribute class





    - Create `com.github.biiiiiigmonster.metamodel.SingularAttribute<E, T>` class in jmodel-metamodel
    - Implement constructor with name, declaringType, and javaType parameters
    - Implement `getName()`, `getDeclaringType()`, `getJavaType()` methods
    - Implement `toString()` to return field name for string conversion
    - Implement `equals()` and `hashCode()` for attribute comparison
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  - [ ]* 2.2 Write property test for SingularAttribute field name round-trip
    - **Property 2: Field Name Preservation (Round-Trip)**
    - **Validates: Requirements 2.1, 2.4**
  - [ ]* 2.3 Write property test for SingularAttribute type information
    - **Property 3: Type Information Preservation**
    - **Validates: Requirements 2.2, 5.1, 5.2, 5.3, 5.4, 5.5**
  - [ ]* 2.4 Write property test for SingularAttribute declaring type
    - **Property 4: Declaring Type Consistency**
    - **Validates: Requirements 2.3**

- [ ] 3. Implement MetamodelGenerator in jmodel-metamodel
  - [x] 3.1 Create MetamodelGenerator class









    - Create `com.github.biiiiiigmonster.metamodel.MetamodelGenerator` class
    - Implement `generate(TypeElement entityElement, ProcessingEnvironment env)` method
    - Implement `extractPersistentFields()` to filter out `@TableField(exist=false)` fields
    - Implement `isPersistentField()` to check field persistence
    - Implement `generateAttributeDeclaration()` for each field
    - Generate both `SingularAttribute` fields and `String` constants (e.g., `ID`, `NAME`)
    - Handle primitive types, wrapper types, date types, enum types, and custom types
    - _Requirements: 1.3, 1.4, 5.1, 5.2, 5.3, 5.4, 5.5_
  - [ ]* 3.2 Write property test for metamodel generation completeness
    - **Property 1: Metamodel Generation Completeness**
    - **Validates: Requirements 1.3, 1.4**
  - [ ]* 3.3 Write property test for non-persistent field exclusion
    - **Property 6: Non-Persistent Field Exclusion**
    - **Validates: Requirements 1.4**

- [ ] 4. Implement ModelMetamodelProcessor in jmodel-metamodel
  - [x] 4.1 Create ModelMetamodelProcessor annotation processor





    - Create `com.github.biiiiiigmonster.metamodel.processor.ModelMetamodelProcessor` class
    - Extend `AbstractProcessor` and implement `process()` method
    - Add `@SupportedAnnotationTypes("*")` and `@SupportedSourceVersion` annotations
    - Implement `isModelSubclass()` to detect Model subclasses by checking type hierarchy
    - Implement `generateMetamodel()` to trigger code generation
    - Add `@Generated` annotation to generated classes with processor info
    - _Requirements: 1.1, 1.2, 4.4_
  - [ ]* 4.2 Write property test for package location consistency
    - **Property 5: Package Location Consistency**
    - **Validates: Requirements 1.2**
  - [ ]* 4.3 Write property test for @Generated annotation presence
    - **Property 7: Generated Annotation Presence**
    - **Validates: Requirements 4.4**

- [ ] 5. Configure ServiceLoader for automatic processor discovery
  - [x] 5.1 Create ServiceLoader configuration





    - Create `jmodel-metamodel/src/main/resources/META-INF/services/javax.annotation.processing.Processor` file
    - Register `com.github.biiiiiigmonster.metamodel.processor.ModelMetamodelProcessor` in the service file
    - _Requirements: 4.1_

- [ ] 6. Checkpoint - Verify metamodel generation works








  - Ensure all tests pass, ask the user if questions arise.
  - Compile jmodel-core and verify metamodel classes are generated for test entities

- [-] 7. Update existing test entities to use generated metamodels




  - [x] 7.1 Update User entity relations to use static attributes

    - Modify `@HasMany` on `posts` field to use `Post_.USER_ID` instead of default
    - Verify User_ metamodel is generated with id, name, email fields
    - _Requirements: 3.1, 3.2_

  - [x] 7.2 Update Post entity relations to use static attributes

    - Modify `@BelongsTo` on `user` field to use `Post_.USER_ID` if applicable
    - Verify Post_ metamodel is generated correctly
    - _Requirements: 3.1, 3.3_
  - [ ] 7.3 Verify all Model entities generate metamodels






    - Ensure Phone_, Profile_, Address_, Role_, Image_, Likes_, Comment_ etc. are generated
    - Verify all persistent fields are included in generated metamodels
    - _Requirements: 1.1, 1.3_

- [ ] 8. Run integration tests and verify functionality
  - [ ] 8.1 Run existing test suite




    - Execute `mvn test` from parent directory
    - Verify all existing tests pass with the new multi-module structure
    - Verify relation loading works correctly with static attribute references
    - _Requirements: 7.5_
  - [ ]* 8.2 Write integration tests for annotation processor
    - Test processor discovers Model subclasses correctly
    - Test generated files are created in correct location
    - Test error handling for edge cases
    - _Requirements: 4.3_

- [ ] 9. Final Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
