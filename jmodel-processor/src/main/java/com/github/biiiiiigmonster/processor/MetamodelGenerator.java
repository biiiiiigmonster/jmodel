package com.github.biiiiiigmonster.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates static metamodel source code for Model entities.
 * <p>
 * This generator creates metamodel classes with static {@code SingularAttribute} fields
 * for each persistent field in the entity, enabling type-safe field references.
 */
public class MetamodelGenerator {

    private static final String TABLE_FIELD_ANNOTATION = "com.baomidou.mybatisplus.annotation.TableField";
    private static final String RELATION_META_ANNOTATION = "com.github.biiiiiigmonster.relation.annotation.config.Relation";
    private static final String SINGULAR_ATTRIBUTE_IMPORT = "com.github.biiiiiigmonster.processor.SingularAttribute";
    private static final String GENERATED_ANNOTATION_IMPORT = "javax.annotation.Generated";

    /**
     * Set of java.lang types that don't need imports.
     */
    private static final Set<String> JAVA_LANG_TYPES = new HashSet<>();

    static {
        JAVA_LANG_TYPES.add("Boolean");
        JAVA_LANG_TYPES.add("Byte");
        JAVA_LANG_TYPES.add("Short");
        JAVA_LANG_TYPES.add("Integer");
        JAVA_LANG_TYPES.add("Long");
        JAVA_LANG_TYPES.add("Character");
        JAVA_LANG_TYPES.add("Float");
        JAVA_LANG_TYPES.add("Double");
        JAVA_LANG_TYPES.add("String");
        JAVA_LANG_TYPES.add("Object");
    }

    /**
     * Mapping of primitive types to their wrapper class names.
     */
    private static final Map<TypeKind, String> PRIMITIVE_TO_WRAPPER = new HashMap<>();

    static {
        PRIMITIVE_TO_WRAPPER.put(TypeKind.BOOLEAN, "Boolean");
        PRIMITIVE_TO_WRAPPER.put(TypeKind.BYTE, "Byte");
        PRIMITIVE_TO_WRAPPER.put(TypeKind.SHORT, "Short");
        PRIMITIVE_TO_WRAPPER.put(TypeKind.INT, "Integer");
        PRIMITIVE_TO_WRAPPER.put(TypeKind.LONG, "Long");
        PRIMITIVE_TO_WRAPPER.put(TypeKind.CHAR, "Character");
        PRIMITIVE_TO_WRAPPER.put(TypeKind.FLOAT, "Float");
        PRIMITIVE_TO_WRAPPER.put(TypeKind.DOUBLE, "Double");
    }

    /**
     * Generates metamodel class source code for an entity.
     *
     * @param entityElement  the type element representing the entity class
     * @param processingEnv  the processing environment
     * @return the generated source code as a string
     */
    public String generate(TypeElement entityElement, ProcessingEnvironment processingEnv) {
        String packageName = getPackageName(entityElement);
        String entitySimpleName = entityElement.getSimpleName().toString();
        String metamodelClassName = entitySimpleName + "_";

        List<VariableElement> persistentFields = extractPersistentFields(entityElement);

        // Collect all imports needed for field types
        Set<String> imports = new HashSet<>();
        imports.add(SINGULAR_ATTRIBUTE_IMPORT);
        imports.add(GENERATED_ANNOTATION_IMPORT);
        
        for (VariableElement field : persistentFields) {
            collectImports(field.asType(), processingEnv, imports, packageName);
        }

        StringBuilder source = new StringBuilder();

        // Package declaration
        if (!packageName.isEmpty()) {
            source.append("package ").append(packageName).append(";\n\n");
        }

        // Imports - sorted for consistency
        List<String> sortedImports = new ArrayList<>(imports);
        sortedImports.sort(String::compareTo);
        for (String importClass : sortedImports) {
            source.append("import ").append(importClass).append(";\n");
        }
        source.append("\n");

        // Class declaration with @Generated annotation
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        source.append("@Generated(\n");
        source.append("    value = \"com.github.biiiiiigmonster.processor.JmodelProcessor\",\n");
        source.append("    date = \"").append(timestamp).append("\"\n");
        source.append(")\n");
        source.append("public abstract class ").append(metamodelClassName).append(" {\n\n");

        // Generate static SingularAttribute fields
        for (VariableElement field : persistentFields) {
            source.append(generateAttributeDeclaration(field, entitySimpleName, processingEnv));
        }

        // Generate string constants for field names
        source.append("\n");
        for (VariableElement field : persistentFields) {
            String fieldName = field.getSimpleName().toString();
            String constantName = toConstantName(fieldName);
            source.append("    public static final String ").append(constantName)
                  .append(" = \"").append(fieldName).append("\";\n");
        }

        // Generate static initializer block
        source.append("\n    static {\n");
        for (VariableElement field : persistentFields) {
            source.append(generateStaticInitializer(field, entitySimpleName, processingEnv));
        }
        source.append("    }\n");

        // Close class
        source.append("}\n");

        return source.toString();
    }

    /**
     * Collects import statements needed for a type.
     *
     * @param typeMirror       the type mirror
     * @param processingEnv    the processing environment
     * @param imports          the set to add imports to
     * @param currentPackage   the current package (types in same package don't need import)
     */
    private void collectImports(TypeMirror typeMirror, ProcessingEnvironment processingEnv, 
                                Set<String> imports, String currentPackage) {
        TypeKind kind = typeMirror.getKind();

        // Primitive types don't need imports (they use wrapper classes from java.lang)
        if (kind.isPrimitive()) {
            return;
        }

        // Handle declared types (classes, interfaces, enums)
        if (kind == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            String qualifiedName = typeElement.getQualifiedName().toString();
            String simpleName = typeElement.getSimpleName().toString();
            
            // Skip java.lang types
            if (qualifiedName.startsWith("java.lang.")) {
                return;
            }
            
            // Skip types in the same package
            String typePackage = getPackageFromQualifiedName(qualifiedName);
            if (typePackage.equals(currentPackage)) {
                return;
            }
            
            // Add import for this type
            imports.add(qualifiedName);
        }
    }

    /**
     * Extracts package name from a fully qualified class name.
     */
    private String getPackageFromQualifiedName(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot > 0) {
            return qualifiedName.substring(0, lastDot);
        }
        return "";
    }


    /**
     * Extracts persistent fields from the entity, excluding fields marked with
     * {@code @TableField(exist = false)}.
     *
     * @param entityElement the entity type element
     * @return list of persistent field elements
     */
    List<VariableElement> extractPersistentFields(TypeElement entityElement) {
        List<VariableElement> persistentFields = new ArrayList<>();

        for (Element enclosedElement : entityElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosedElement;
                
                // Skip static fields
                if (field.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }

                if (isPersistentField(field)) {
                    persistentFields.add(field);
                }
            }
        }

        return persistentFields;
    }

    /**
     * Checks if a field should be included in the metamodel.
     * A field is persistent if it does not have {@code @TableField(exist = false)}.
     * Fields with @Relation annotations are also included.
     *
     * @param field the field element to check
     * @return true if the field is persistent, false otherwise
     */
    boolean isPersistentField(VariableElement field) {
        // If field has @Relation annotation, include it
        if (hasRelationAnnotation(field)) {
            return true;
        }
        
        // Look for @TableField annotation
        for (javax.lang.model.element.AnnotationMirror annotationMirror : field.getAnnotationMirrors()) {
            String annotationType = annotationMirror.getAnnotationType().toString();
            
            if (TABLE_FIELD_ANNOTATION.equals(annotationType)) {
                // Check the 'exist' attribute
                for (java.util.Map.Entry<? extends javax.lang.model.element.ExecutableElement, 
                        ? extends javax.lang.model.element.AnnotationValue> entry 
                        : annotationMirror.getElementValues().entrySet()) {
                    String key = entry.getKey().getSimpleName().toString();
                    if ("exist".equals(key)) {
                        Object value = entry.getValue().getValue();
                        if (Boolean.FALSE.equals(value)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Checks if a field has an annotation that is meta-annotated with @Relation.
     *
     * @param field the field element to check
     * @return true if the field has a @Relation meta-annotated annotation
     */
    private boolean hasRelationAnnotation(VariableElement field) {
        for (AnnotationMirror annotationMirror : field.getAnnotationMirrors()) {
            // Get the annotation type element
            Element annotationElement = annotationMirror.getAnnotationType().asElement();
            
            // Check if this annotation is meta-annotated with @Relation
            for (AnnotationMirror metaAnnotation : annotationElement.getAnnotationMirrors()) {
                String metaAnnotationType = metaAnnotation.getAnnotationType().toString();
                if (RELATION_META_ANNOTATION.equals(metaAnnotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generates a static attribute declaration for a field.
     *
     * @param field           the field element
     * @param entityClassName the simple name of the entity class
     * @param processingEnv   the processing environment
     * @return the attribute declaration source code
     */
    String generateAttributeDeclaration(VariableElement field, String entityClassName, 
                                        ProcessingEnvironment processingEnv) {
        String fieldName = field.getSimpleName().toString();
        String fieldTypeClass = getTypeClassName(field.asType(), processingEnv);

        return String.format("    public static volatile SingularAttribute<%s, %s> %s;\n",
                entityClassName, fieldTypeClass, fieldName);
    }

    /**
     * Generates the static initializer statement for a field.
     *
     * @param field           the field element
     * @param entityClassName the simple name of the entity class
     * @param processingEnv   the processing environment
     * @return the static initializer source code
     */
    private String generateStaticInitializer(VariableElement field, String entityClassName,
                                             ProcessingEnvironment processingEnv) {
        String fieldName = field.getSimpleName().toString();
        String fieldTypeClass = getTypeClassName(field.asType(), processingEnv);

        return String.format("        %s = new SingularAttribute<>(\"%s\", %s.class, %s.class);\n",
                fieldName, fieldName, entityClassName, fieldTypeClass);
    }

    /**
     * Gets the class name for a type, handling primitives, wrappers, and complex types.
     *
     * @param typeMirror    the type mirror
     * @param processingEnv the processing environment
     * @return the class name suitable for use in generated code
     */
    private String getTypeClassName(TypeMirror typeMirror, ProcessingEnvironment processingEnv) {
        TypeKind kind = typeMirror.getKind();

        // Handle primitive types - convert to wrapper
        if (kind.isPrimitive()) {
            String wrapperName = PRIMITIVE_TO_WRAPPER.get(kind);
            if (wrapperName != null) {
                return wrapperName;
            }
            // Fallback: box the primitive
            TypeElement boxedType = processingEnv.getTypeUtils().boxedClass((PrimitiveType) typeMirror);
            return boxedType.getSimpleName().toString();
        }

        // Handle declared types (classes, interfaces, enums)
        if (kind == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            return typeElement.getSimpleName().toString();
        }

        // Handle arrays
        if (kind == TypeKind.ARRAY) {
            return "Object"; // Fallback for arrays
        }

        // Fallback for unknown types
        return "Object";
    }

    /**
     * Converts a field name to a constant name (uppercase with underscores).
     *
     * @param fieldName the field name in camelCase
     * @return the constant name in UPPER_SNAKE_CASE
     */
    private String toConstantName(String fieldName) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('_');
            }
            result.append(Character.toUpperCase(c));
        }
        return result.toString();
    }

    /**
     * Gets the package name for an entity element.
     *
     * @param entityElement the entity type element
     * @return the package name, or empty string if in default package
     */
    private String getPackageName(TypeElement entityElement) {
        String qualifiedName = entityElement.getQualifiedName().toString();
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot > 0) {
            return qualifiedName.substring(0, lastDot);
        }
        return "";
    }

    /**
     * Gets the fully qualified name for the generated metamodel class.
     *
     * @param entityElement the entity type element
     * @return the fully qualified metamodel class name
     */
    public String getMetamodelClassName(TypeElement entityElement) {
        String packageName = getPackageName(entityElement);
        String entitySimpleName = entityElement.getSimpleName().toString();
        if (packageName.isEmpty()) {
            return entitySimpleName + "_";
        }
        return packageName + "." + entitySimpleName + "_";
    }
}
